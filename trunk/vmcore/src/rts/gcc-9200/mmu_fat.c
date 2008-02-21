/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#include "cache.h"
#include "system.h"
#include "flash.h"
/*
 * This file contains routines that query the contents of the FlashFile FAT 
 * and set up the MMU to match the virtual file settings in the FAT.
 * 
 * To understand this file you will need an external source that explains the 
 * ARM9 MMU. We used and recommend "ARM System Developer's Guide" by Sloss, Symes
 * and Wright. 
 */
 
// FAT constants: these must match the constants in FlashManager.java and ConfigPage.java
#define VIRTUAL_ADDRESS_FILE_COUNT			8
#define FAT_SECTOR							5
#define SECTOR_SIZE							0x10000
#define VIRTUAL_ADDRESS_FILE_SPACING 		(1024*1024)
#define VIRTUAL_ADDRESS_SPACE_LOWER_BOUND 	0x10800000
#define VIRTUAL_ADDRESS_SPACE_UPPER_BOUND 	(VIRTUAL_ADDRESS_SPACE_LOWER_BOUND + (VIRTUAL_ADDRESS_FILE_COUNT*VIRTUAL_ADDRESS_FILE_SPACING))
#define FAT_IDENTIFIER_V1					0x12345678
#define FAT_IDENTIFIER_V2					0x12345679

// MMU mapping tables
#define LEVEL_2_TABLE_ENTRIES				(256)
#define LEVEL_2_TABLE_SIZE					(sizeof(int)*LEVEL_2_TABLE_ENTRIES)

#define LEVEL_1_PAGE_TABLE_SIZE				(16 * 1024)
#define LEVEL_2_PAGE_TABLE_SIZE				(LEVEL_2_TABLE_SIZE * VIRTUAL_ADDRESS_FILE_COUNT)

// The page tables must fit inside the MMU_SPACE (see system.h)
#define PAGE_TABLE_SIZE 					(LEVEL_2_PAGE_TABLE_SIZE)

// This must match the constant in FlashFileDescriptor.java
#define OBSOLETE_FLAG_MASK					0x1

#define LEVEL_2_PAGE_TABLE_ADDR				(STACK)
// level 1 table in sectors 6 and 7
#define LEVEL_1_PAGE_TABLE_ADDR				(FLASH_BASE_ADDR+0x00C000)

#define NUMBER_OF_64K_PAGES_IN_1MB 16

static unsigned int* get_address_of_level_2_table_containing(unsigned int virtual_address) {
	unsigned int megabyte = (virtual_address - VIRTUAL_ADDRESS_SPACE_LOWER_BOUND) / (1024*1024);
	return (unsigned int *)(LEVEL_2_PAGE_TABLE_ADDR+(LEVEL_2_TABLE_SIZE*megabyte));
}

static void map_level_2_entry_using_addresses(int virtual_address, int physical_address) {
	int j;
	unsigned int* level_2_table = get_address_of_level_2_table_containing(virtual_address);
	unsigned int first_level_2_table_entry = (virtual_address >> 12) & 0xFF; 
	
	// for "coarse" page tables we need 16 identical entries in a row
	for (j=0; j<16; j++) {
		// 31..16 physical base address
		// 15.12 unused (=0)
		// 11..10 access permission for first 16k (=11, allow all)
		// 9..8 access permission for second 16k (=11, allow all)
		// 7..6 access permission for third 16k (=11, allow all)
		// 5..4 access permission for fourth 16k (=11, allow all)
		// 3..2 C & B cache bits (=10, write-through caching)
		// 1..0 entry type (=01, large page of 64k)
		level_2_table[first_level_2_table_entry+j] = physical_address | 0xFF9;
	}
}

/*
 * Initialise the MMU using the level one page table stored in flash.
 */
void mmu_enable(void) {
	// enable access to all domains
	AT91_coprocessor15_3(0, 0xFFFFFFFF); 
	
	// set MMU translation table base address
	AT91_coprocessor15_2(0xFFFFFFFF, LEVEL_1_PAGE_TABLE_ADDR); 
	
	// turn MMU on
	AT91_coprocessor15_1(0, 1<<0); 
}

/*
 * Initialise the page table that controls the MMU's operation. The page table is created in RAM,
 * and then compared to a copy held in flash, which is overwritten only if it
 * is different. The RAM copy is then discarded. This strategy means that
 * the top level page table - which doesn't change in normal operation - is not
 * wasting valuable RAM space. 
 */
void page_table_init() {
	int i, j;
	unsigned int level_1_table[4096];
	
	// map virtual to physical for 4GB and make uncacheable
	for (i=0; i<4096; i++) {
		// 31..20 physical base address
		// 19..12 unused (=0)
		// 11.10 access perms (=11, allow all)
		// 9 unused (=0)
		// 8..5 domain (=0)
		// 4 unused (=1)
		// 3..2 C & B cache bits (=0, don't cache)
		// 1..0 entry type (=10 for section descriptor)
		level_1_table[i] = (i<<20) | 0xC12;
	}

	// turn on caching for RAM (512KB, but we're using 1MB MMU pages here)
	level_1_table[0x200] |= 0xC; // enable write-back caching

	// All directly mapped flash areas
	for (i=0; i<4; i++) {
		level_1_table[0x100+i] |= 0x8; // write-through caching
	}
	
	level_1_table[UNCACHED_RAM_START_ADDDRESS >> 20] = 0x20000000 | 0xC12;

	// Set up tables for virtual files
	for (j = 0; j < VIRTUAL_ADDRESS_FILE_COUNT; ++j) {
		level_1_table[(VIRTUAL_ADDRESS_SPACE_LOWER_BOUND >> 20) + j] = 
			(LEVEL_2_PAGE_TABLE_ADDR+(LEVEL_2_TABLE_SIZE*j)) | 0x11; // subdivide this 1Mb of flash
		for (i=0; i<NUMBER_OF_64K_PAGES_IN_1MB; i++) {
			// map to itself - will cause a memory access fault if not overwritten
			map_level_2_entry_using_addresses(
				VIRTUAL_ADDRESS_SPACE_LOWER_BOUND+(j*VIRTUAL_ADDRESS_FILE_SPACING)+(i*SECTOR_SIZE),
				VIRTUAL_ADDRESS_SPACE_LOWER_BOUND+(j*VIRTUAL_ADDRESS_FILE_SPACING)+(i*SECTOR_SIZE)); 
		}
	}
	
	unsigned int* level_1_table_in_flash = (unsigned int*)LEVEL_1_PAGE_TABLE_ADDR;
	int need_to_flash = FALSE;
	for (i=0; i<4096; i++) {
		if (level_1_table[i] != level_1_table_in_flash[i]) {
			need_to_flash = TRUE;
			break;
		}
	}
	if (need_to_flash) {
		sysPrint("Updating MMU table\r\n");
		flash_write_with_erase((unsigned char*)level_1_table, 4096*4, (Flash_ptr)LEVEL_1_PAGE_TABLE_ADDR);
	}
}

static int read_number(unsigned char* ptr, int number_of_bytes) {
	int result = 0;
	int i;
	for (i=0; i<number_of_bytes; i++) {
		result = (result << 8) | *(ptr+i);
	}
	return result;
}

static int is_FAT_V2;

static int is_FAT_valid() {
	int fat_id = read_number((char*)get_sector_address(FAT_SECTOR), 4);
	is_FAT_V2 = FALSE;
	switch (fat_id) {
		case FAT_IDENTIFIER_V2:
			is_FAT_V2 = TRUE;
		case FAT_IDENTIFIER_V1:
			return TRUE;
		default:
			return FALSE;
	}
}

static int get_allocated_file_size_V2(int required_virtual_address) {
}

static int get_allocated_file_size_V1(int required_virtual_address) {
	int i, j;
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier

	int file_count = read_number(fat_ptr, 2);
	fat_ptr += 2;
	for (i = 0; i < file_count; ++i) {
		int flags = read_number(fat_ptr, 2);
		fat_ptr += 2;
		int is_obsolete = flags & OBSOLETE_FLAG_MASK;
		unsigned int virtual_address = read_number(fat_ptr, 4);
		fat_ptr += 4;
		int sector_count = read_number(fat_ptr, 2);
		if (virtual_address == required_virtual_address && !is_obsolete) {
			return sector_count * SECTOR_SIZE;
		}
		fat_ptr += 2;
		fat_ptr += 2 * sector_count;

		fat_ptr += 2 + read_number(fat_ptr, 2); // skip name
		fat_ptr += 4; // skip size
		fat_ptr += 8; // skip time modified (a long)
		fat_ptr += 2 + read_number(fat_ptr, 2); // skip comment
	}
	return -1;
}

/*
 * Answer the space (in bytes) allocated to the FlashFile that occupies
 * the given virtual address. If there is no such file, answers -1.
 * 
 * required_virtual_address   Virtual address of required FlashFile
 */
int get_allocated_file_size(int required_virtual_address) {
	if (!is_FAT_valid()) {
		return -1;
	}
	if (is_FAT_V2) {
		return get_allocated_file_size_V2(required_virtual_address);
	} else {
		return get_allocated_file_size_V1(required_virtual_address);
	}
}

static unsigned int get_file_virtual_address_V2(int target_file_name_length, char* target_file_name) {
}

static unsigned int get_file_virtual_address_V1(int target_file_name_length, char* target_file_name) {
	int i, j;
	if (!is_FAT_valid()) {
		return -1;
	}
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier

	int file_count = read_number(fat_ptr, 2);
	fat_ptr += 2;
	for (i = 0; i < file_count; ++i) {
		int flags = read_number(fat_ptr, 2);
		fat_ptr += 2;
		int is_obsolete = flags & OBSOLETE_FLAG_MASK;
		unsigned int virtual_address = read_number(fat_ptr, 4);
		fat_ptr += 4;
		int sector_count = read_number(fat_ptr, 2);
		fat_ptr += 2;
		fat_ptr += 2 * sector_count;

		int file_name_length = read_number(fat_ptr, 2);
		fat_ptr += 2;
		if (!is_obsolete && (file_name_length == target_file_name_length)) {
			if (strncmp(target_file_name, fat_ptr, file_name_length) == 0) {
				return virtual_address;
			}
		}
		fat_ptr += file_name_length;

		fat_ptr += 4; // skip size
		fat_ptr += 8; // skip time modified (a long)
		fat_ptr += 2 + read_number(fat_ptr, 2); // skip comment
	}
	return -1;
}

/*
 * Answer the virtual address of the flash file with a specified name. If
 * there is no such file, answers -1.
 * 
 * target_file_name_length    Length of the file name
 * target_file_name           Address of the buffer containing the file name
 */
unsigned int get_file_virtual_address(int target_file_name_length, char* target_file_name) {
	if (!is_FAT_valid()) {
		return -1;
	}
	if (is_FAT_V2) {
		return get_file_virtual_address_V2(target_file_name_length, target_file_name);
	} else {
		return get_file_virtual_address_V1(target_file_name_length, target_file_name);
	}
}

static int reprogram_mmu_V2(int ignore_obsolete_files) {
}

static int reprogram_mmu_V1(int ignore_obsolete_files) {
	int i, j;
	if (!is_FAT_valid()) {
		return FALSE;
	}
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier

	int file_count = read_number(fat_ptr, 2);
	fat_ptr += 2;
	for (i = 0; i < file_count; ++i) {
		int flags = read_number(fat_ptr, 2);
		fat_ptr += 2;
		int is_obsolete = flags & OBSOLETE_FLAG_MASK;
		unsigned int virtual_address = read_number(fat_ptr, 4);
		fat_ptr += 4;
		int sector_count = read_number(fat_ptr, 2);
		fat_ptr += 2;
		for (j = 0; j < sector_count; ++j) {
			int sector_number = read_number(fat_ptr, 2);
			fat_ptr += 2;
			if (virtual_address != 0 && !(is_obsolete && ignore_obsolete_files)) {
				map_level_2_entry_using_addresses(virtual_address, get_sector_address(sector_number));
				virtual_address += SECTOR_SIZE;
			}
		}
		fat_ptr += 2 + read_number(fat_ptr, 2); // skip name
		fat_ptr += 4; // skip size
		fat_ptr += 8; // skip time modified (a long)
		fat_ptr += 2 + read_number(fat_ptr, 2); // skip comment
	}
	data_cache_disable();
	invalidate_data_tlb();
	data_cache_enable();
	return TRUE;
}

/*
 * Reprogram the MMU to map files into virtual memory as implied 
 * by the virtual memory addresses specified in the FAT. Answer whether
 * a valid FAT was detected (if not, the MMU is left untouched).
 * 
 * ignore_obsolete_files    specify whether or not to map obsolete files
 */
int reprogram_mmu(int ignore_obsolete_files) {
	if (!is_FAT_valid()) {
		return -1;
	}
	if (is_FAT_V2) {
		return reprogram_mmu_V2(ignore_obsolete_files);
	} else {
		return reprogram_mmu_V1(ignore_obsolete_files);
	}
}

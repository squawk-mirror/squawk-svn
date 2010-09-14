package com.sun.squawk.test;

public class Work {

	final static int DEBUG = -1;
	final static int FLAT_ADD = 0;
	final static int RECUR_CALL = FLAT_ADD + 1;
	final static int RECUR_CALL_W_ADD = RECUR_CALL + 1;
	final static int NATIVE_CALL = RECUR_CALL_W_ADD + 1;
	final static int LIB_CALL = NATIVE_CALL + 1;

	final static int X1 = 1;
	final static int X4 = 4;
	final static int X16 = 16;
	final static int X64 = 64;
	final static int X256 = 256;
	final static int X1024 = 1024;
	final static int X4096 = 4096;
	final static int X16384 = 16384;
	final static int X65536 = 65536;
	final static int X131072 = 131072;
	final static int X262144 = 262144;
	final static int X524288 = 524288;
	final static int X1048576 = 1048576;
	final static int X2097152 = 2097152;
	final static int X4194304 = 4194304;
	final static int X33554432 = 33554432;

	int type;
	int workload;

	public Work(int type, int workload) {
		this.type = type;
		this.workload = workload;
	}

	static String typeToString(int type) {
		switch (type) {
		case FLAT_ADD:
			return "flat add";
		case RECUR_CALL:
			return "recursive call";
		case RECUR_CALL_W_ADD:
			return "recursive call with some adds";
		case NATIVE_CALL:
			return "native call";
		case LIB_CALL:
			return "lib call";
		case DEBUG:
			return "debug";
		default:
			return "Unknown";
		}
	}

	void doIt() {
		switch (type) {
		case FLAT_ADD:
			doFlatAdd();
			break;
		case RECUR_CALL:
			doRecurCall();
			break;
		case RECUR_CALL_W_ADD:
			doRecurCallWithAdds();
			break;
		case NATIVE_CALL:
			doNativeCall();
			break;
		case LIB_CALL:
			doLibCall();
			break;
		case DEBUG:
			doDebug();
			break;
		default:
			System.err.println("Unknown job type: " + type);
		}
	}

	private void doRecurCallWithAdds() {
		recurAdd1(0, workload);
	}

	private int recurAdd1(int val, int count) {
		flatAdd_1024x();
		if (count == 0) {
			return val;
		} else {
			return recurAdd1(val + 1, count - 1);
		}
	}

	private void doRecurCall() {
		recurAdd(0, workload);
	}

	private int recurAdd(int val, int count) {
		if (count == 0) {
			return val;
		} else {
			return recurAdd(val + 1, count - 1);
		}
	}

	private void doNativeCall() {
		// TODO Auto-generated method stub

	}

	private void doLibCall() {
		// TODO Auto-generated method stub

	}

	private void doDebug() {
		// nothing to do
	}

	private void doFlatAdd() {
		switch (workload) {
		case X1:
			flatAdd_1x();
			break;
		case X4:
			flatAdd_4x();
			break;
		case X16:
			flatAdd_16x();
			break;
		case X64:
			flatAdd_64x();
			break;
		case X256:
			flatAdd_256x();
			break;
		case X1024:
			flatAdd_1024x();
			break;
		case X4096:
			flatAdd_4096x();
			break;
		case X16384:
			flatAdd_16384x();
			break;
		case X65536:
			flatAdd_65536x();
			break;
		case X131072:
			flatAdd_131072x();
			break;
		case X262144:
			flatAdd_262144x();
			break;
		case X524288:
			flatAdd_524288x();
			break;
		case X1048576:
			flatAdd_1048576x();
			break;
		case X2097152:
			flatAdd_2097152x();
			break;
		case X4194304:
			flatAdd_4194304x();
			break;
		case X33554432:
			flatAdd_33554432x();
			break;
		}

	}

	private void flatAdd_33554432x() {
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
		flatAdd_4194304x();
	}

	private void flatAdd_4194304x() {
		flatAdd_2097152x();
		flatAdd_2097152x();
	}

	private void flatAdd_2097152x() {
		flatAdd_1048576x();		
		flatAdd_1048576x();		
	}

	private void flatAdd_1048576x() {
		flatAdd_524288x();
		flatAdd_524288x();
	}

	private void flatAdd_524288x() {
		flatAdd_262144x();
		flatAdd_262144x();
	}

	private void flatAdd_262144x() {
		flatAdd_131072x();
		flatAdd_131072x();
	}

	private void flatAdd_131072x() {
		flatAdd_65536x();
		flatAdd_65536x();
	}

	private void flatAdd_65536x() {
		flatAdd_16384x();
		flatAdd_16384x();
		flatAdd_16384x();
		flatAdd_16384x();
	}

	private void flatAdd_16384x() {
		flatAdd_4096x();
		flatAdd_4096x();
		flatAdd_4096x();
		flatAdd_4096x();
	}

	private void flatAdd_4096x() {
		flatAdd_1024x();
		flatAdd_1024x();
		flatAdd_1024x();
		flatAdd_1024x();
	}

	private void flatAdd_1024x() {
		flatAdd_256x();
		flatAdd_256x();
		flatAdd_256x();
		flatAdd_256x();
	}

	private void flatAdd_256x() {
		flatAdd_64x();
		flatAdd_64x();
		flatAdd_64x();
		flatAdd_64x();
	}

	private void flatAdd_64x() {
		flatAdd_16x();
		flatAdd_16x();
		flatAdd_16x();
		flatAdd_16x();
	}

	private void flatAdd_16x() {
		flatAdd_4x();
		flatAdd_4x();
		flatAdd_4x();
		flatAdd_4x();
	}

	private void flatAdd_4x() {
		flatAdd_1x();
		flatAdd_1x();
		flatAdd_1x();
		flatAdd_1x();
	}

	private void flatAdd_1x() {
		int s = 0;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
		s++;
	}
}

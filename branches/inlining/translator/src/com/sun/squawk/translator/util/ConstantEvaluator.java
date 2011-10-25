 /*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * This is a part of the Squawk JVM translator.
 */
package com.sun.squawk.translator.util;

import com.sun.squawk.util.Assert;
import com.sun.squawk.vm.OPC;

/**
 * The ConstantEvaluator class performs simple evaluation of constant objects. It is used by the IROptimizer, 
 * but it is not translator specific. It could be used by the compiler.
 * 
 * The evaluator accepts the primitive wrapper classes Integer, Long, Float, and Double, and returns such objects. 
 * In a J2SE system we would use class java.lang.Number as the argument's type, but we must use Object in J2ME.
 *
 * The evaluator supports the following operations:
 *    Binary
 *    + - * / % << >> >>> & | ^
 *
 *    Unary
 *    - I2B I2C I2S I2L I2F I2D L2I L2F L2D F2I F2L F2D D2I D2L D2F
 *
 *    Binary comparison ( left OP right) for standard ops: EQ, NE, LT, LE, GE, GT
 *
 *    Unary comparison ( left OP zero) for standard ops: EQ, NE, LT, LE, GE, GT
 * 
 * @author Derek White
 */
public class ConstantEvaluator {
    
    /**
     * Evaluate performing the arithmetic operation <code>opcode</code>
     * on two constants or the same type. Returns an Integer, Long, Float, or Double.
     * evaluateBinary will return null if the evaluation would cause a divide by zero exception (ArithmeticException).
     *
     * @param left the left operand
     * @param right the right operand
     * @param opcode the Squawk opcode corresponding to the operation
     * @return the resulting constant value, or null if the operation cannot be performed at translation time.
     */
    public static Object evaluateBinary(Object left, Object right, int opcode) {
        Object newValue = null;
                    
        if (left instanceof Integer) {
            int l = ((Integer)left).intValue();
            int r = ((Integer)right).intValue();
            switch (opcode) {
                case OPC.ADD_I:
                    newValue = new Integer(l + r);
                    break;
                case OPC.SUB_I:
                    newValue = new Integer(l - r);
                    break;
                case OPC.MUL_I:
                    newValue = new Integer(l * r);
                    break;
                case OPC.DIV_I:
                    if (r == 0) {
                        newValue = null;
                    } else {
                        newValue = new Integer(l / r);
                    }
                    break;
                case OPC.REM_I:
                    if (r == 0) {
                        newValue = null;
                    } else {
                        newValue = new Integer(l % r);
                    }
                    break;
                case OPC.SHL_I:
                    newValue = new Integer(l << r);
                    break;
                case OPC.SHR_I:
                    newValue = new Integer(l >> r);
                    break;
                case OPC.USHR_I:
                    newValue = new Integer(l >>> r);
                    break;
                case OPC.AND_I:
                    newValue = new Integer(l & r);
                    break;
                case OPC.OR_I:
                    newValue = new Integer(l | r);
                    break;
                case OPC.XOR_I:
                    newValue = new Integer(l ^ r);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
        } else if (left instanceof Long) {
            long l = ((Long)left).longValue();
            long r;
            if (opcode == OPC.SHL_L ||
                opcode == OPC.SHR_L ||
                opcode == OPC.USHR_L) {
                r = ((Integer)right).intValue();
            } else {
                r = ((Long)right).longValue();
            }
            switch (opcode) {
                case OPC.ADD_L:
                    newValue = new Long(l + r);
                    break;
                case OPC.SUB_L:
                    newValue = new Long(l - r);
                    break;
                case OPC.MUL_L:
                    newValue = new Long(l * r);
                    break;
                case OPC.DIV_L:
                    if (r == 0) {
                        newValue = null;
                    } else {
                        newValue = new Long(l / r);
                    }
                    break;
                case OPC.REM_L:
                    if (r == 0) {
                        newValue = null;
                    } else {
                        newValue = new Long(l % r);
                    }
                    break;
                case OPC.SHL_L:
                    newValue = new Long(l << r);
                    break;
                case OPC.SHR_L:
                    newValue = new Long(l >> r);
                    break;
                case OPC.USHR_L:
                    newValue = new Long(l >>> r);
                    break;
                case OPC.AND_L:
                    newValue = new Long(l & r);
                    break;
                case OPC.OR_L:
                    newValue = new Long(l | r);
                    break;
                case OPC.XOR_L:
                    newValue = new Long(l ^ r);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
/*if[FLOATS]*/
         } else if (left instanceof Float) {
            float l = ((Float)left).floatValue();
            float r = ((Float)right).floatValue();
            switch (opcode) {
                case OPC.ADD_F:
                    newValue = new Float(l + r);
                    break;
                case OPC.SUB_F:
                    newValue = new Float(l - r);
                    break;
                case OPC.MUL_F:
                    newValue = new Float(l * r);
                    break;
                case OPC.DIV_F:
                    newValue = new Float(l / r);
                    break;
                case OPC.REM_F:
                    newValue = new Float(l % r);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
        } else if (left instanceof Double) {
            double l = ((Double)left).doubleValue();
            double r = ((Double)right).doubleValue();
            switch (opcode) {
                case OPC.ADD_D:
                    newValue = new Double(l + r);
                    break;
                case OPC.SUB_D:
                    newValue = new Double(l - r);
                    break;
                case OPC.MUL_D:
                    newValue = new Double(l * r);
                    break;
                case OPC.DIV_D:
                    newValue = new Double(l / r);
                    break;
                case OPC.REM_D:
                    newValue = new Double(l % r);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
/*end[FLOATS]*/ 
        } else {
            Assert.shouldNotReachHere();
        }
        
        return newValue;
    }
    
    
    /**
     * Evaluate performing the unary arithmetic operation <code>opcode</code>
     * on a constant. Returns an Integer, Long, Float, or Double.
     *
     * @param c the operand
     * @param opcode the Squawk opcode corresponding to the operation
     * @return the resulting constant value.
     */
    public static Object evaluateUnary(Object c, int opcode) {
        Object newValue = null;

        if (c instanceof Integer) {
            int v = ((Integer)c).intValue();
            switch (opcode) {
                case OPC.NEG_I:
                    newValue = new Integer(-v);
                    break;
                case OPC.I2B:
                    newValue = new Integer((byte)v);
                    break;
                case OPC.I2S:
                    newValue = new Integer((short)v);
                    break;
                case OPC.I2C:
                    newValue = new Integer((char)v);
                    break;
                case OPC.I2L:
                    newValue = new Long((long)v);
                    break;
/*if[FLOATS]*/
                case OPC.I2F:
                    newValue = new Float((float)v);
                    break;
                case OPC.I2D:
                    newValue = new Double((double)v);
                    break;
/*end[FLOATS]*/
                default:
                    Assert.shouldNotReachHere();
            }
        } else if (c instanceof Long) {
            long v = ((Long)c).longValue();
            switch (opcode) {
                case OPC.NEG_L:
                    newValue = new Long(-v);
                    break;
                case OPC.L2I:
                    newValue = new Integer((int)v);
                    break;
/*if[FLOATS]*/
                case OPC.L2F:
                    newValue = new Float((float)v);
                    break;
                case OPC.L2D:
                    newValue = new Double((double)v);
                    break;
/*end[FLOATS]*/                   
                 default:
                    Assert.shouldNotReachHere();
            }
/*if[FLOATS]*/
         } else if (c instanceof Float) {
            float v = ((Float)c).floatValue();
            
            switch (opcode) {
                case OPC.NEG_F:
                    newValue = new Float(-v);
                    break;
                case OPC.F2I:
                    newValue = new Integer((int)v);
                    break;
                case OPC.F2L:
                    newValue = new Long((long)v);
                    break;
                case OPC.F2D:
                    newValue = new Double((double)v);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
        } else if (c instanceof Double) {
            double v = ((Double)c).doubleValue();
            switch (opcode) {
                case OPC.NEG_D:
                    newValue = new Double(-v);
                    break;
                case OPC.D2I:
                    newValue = new Integer((int)v);
                    break;
                case OPC.D2L:
                    newValue = new Long((long)v);
                    break;
                case OPC.D2F:
                    newValue = new Float((float)v);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
/*end[FLOATS]*/ 
        } else {
            Assert.shouldNotReachHere();
        }
        
        return newValue;
    }
    
    /**
     * Evaluate a numeric comparison test of the argument c1 to the argument c2.
     *
     * @param c1 the left value to test. Must be one of Integer, Long, Float, or Double.
     * @param c2 the right value to test. Must be one of Integer, Long, Float, or Double.
     * @param opcode the Squawk opcode corresponding to the operation
     * @return true if the test succeeded.
     */
    public static boolean doConstTest(Object c1, Object c2, int opcode) {
        Assert.that(c1.getClass() == c2.getClass());
        switch (opcode) {
            case OPC.IF_CMPEQ_I:
            case OPC.IF_CMPEQ_L:
                return EQ(c1, c2);

            case OPC.IF_CMPNE_I:
            case OPC.IF_CMPNE_L:
                return NE(c1, c2);
            
            case OPC.IF_CMPLT_I:
            case OPC.IF_CMPLT_L:
                return LT(c1, c2);
                
            case OPC.IF_CMPLE_I:
            case OPC.IF_CMPLE_L:
                return LE(c1, c2);
                
            case OPC.IF_CMPGE_I:
            case OPC.IF_CMPGE_L:
                return GE(c1, c2);
                
            case OPC.IF_CMPGT_I:
            case OPC.IF_CMPGT_L:
                return GT(c1, c2);
/*if[FLOATS]*/
/*end[FLOATS]*/
        }
         Assert.shouldNotReachHere();
         return false;
    }
    
    /**
     * Evaluate a numeric comparison test of the argument c to it's canonical zero value.
     *
     * @param c1 the value to test. Must be one of Integer, Long, Float, or Double.
     * @param opcode the Squawk opcode corresponding to the operation
     * @return true if the test succeeded.
     */
    public static boolean doConstTest(Object c, int opcode) {
        Object zeroObj = getZero(c);
        switch (opcode) {
            case OPC.IF_EQ_I:
            case OPC.IF_EQ_L:
                return EQ(c, zeroObj);
                
            case OPC.IF_NE_I:
            case OPC.IF_NE_L:
                return NE(c, zeroObj);
            
            case OPC.IF_LT_I:
            case OPC.IF_LT_L:
                return LT(c, zeroObj);
                
            case OPC.IF_LE_I:
            case OPC.IF_LE_L:
                return LE(c, zeroObj);
                
            case OPC.IF_GE_I:
            case OPC.IF_GE_L:
                return GE(c, zeroObj);
                
            case OPC.IF_GT_I:
            case OPC.IF_GT_L:
                return GT(c, zeroObj);
/*if[FLOATS]*/
/*end[FLOATS]*/ 
        }
        Assert.shouldNotReachHere();
        return false;
    }
    
    /**********************************************************************
     *              Compare operations for primitive wrapper classes
     *              
     *  These operations are equivalent to Number.compareTo() in J2SE.
     **********************************************************************/
    
    /**
     * The canonical "zero" values for these types.
     */
    private final static Integer DEFAULT_INTEGER = new Integer(0);
    private final static Long DEFAULT_LONG = new Long(0);

/*if[FLOATS]*/
    private final static Float DEFAULT_FLOAT = new Float(0.0);
    private final static Double DEFAULT_DOUBLE = new Double(0.0);
/*end[FLOATS]*/
    
    /**
     * Return the canonical zero value for an object of the given type:
     *
     * @param obj an Integer, Long, Float, or Double object
     * @return an Integer, Long, Float, or Double object with the value of zero.
     */
    private static Object getZero(Object obj) {
        if (obj instanceof Integer) {
            return DEFAULT_INTEGER;
        } else if (obj instanceof Long) {
            return DEFAULT_LONG;
/*if[FLOATS]*/
        } else if (obj instanceof Float) {
            return DEFAULT_FLOAT;
        } else if (obj instanceof Double) {
            return DEFAULT_DOUBLE;
/*end[FLOATS]*/
        }
        throw new IllegalArgumentException("Unsupported type: " + obj.getClass());
    }
    
    /**
     * Compare two Constant objects with identical types.
     *
     * @param c the Constant to be compared. 
     * @return  the value 0 if obj1 is equal to the argument obj2;
     *          -1 if obj1 is numerically less than obj2;
     *          +1 if obj1 is numerically greater than obj2 (signed comparison).
     *          +2 if either operand is a NaN.
     */
    public static int compare(Object obj1, Object obj2) {
        if (obj1.getClass() != obj2.getClass()) {
            throw new IllegalArgumentException("Arguments must have the same type: " + obj1 + " != " + obj2);
        }
        
        if (obj1 instanceof Integer) {
            int val1 = ((Integer)obj1).intValue();
            int val2 = ((Integer)obj2).intValue();
            if (val1 < val2) {
                return -1;
            } else if (val1 == val2) { 
                return 0;
            } else {
                return 1;
            }
        } else if (obj1 instanceof Long) {
            long val1 = ((Long)obj1).longValue();
            long val2 = ((Long)obj2).longValue();
            if (val1 < val2) {
                return -1;
            } else if (val1 == val2) { 
                return 0;
            } else {
                return 1;
            }
/*if[FLOATS]*/
        } else if (obj1 instanceof Float) {
            float val1 = ((Float)obj1).floatValue();
            float val2 = ((Float)obj2).floatValue();
            if (val1 < val2) {
                return -1;
            } else if (val1 == val2) { 
                return 0;
            }
            if (val1 > val2) {
                return 1;
            } else {
                return 2; // NAN!!!!
            }
        } else if (obj1 instanceof Double) {
            double val1 = ((Double)obj1).doubleValue();
            double val2 = ((Double)obj2).doubleValue();
            if (val1 < val2) {
                return -1;
            } else if (val1 == val2) { 
                return 0;
            }
            if (val1 > val2) {
                return 1;
            } else {
                return 2; // NAN!!!!
            }
/*end[FLOATS]*/
        }
        throw new IllegalArgumentException("Unsupported type: " + obj1.getClass());
    }
    
    /**
     * Return true if <code>this</code> and <code>c</code>
     * have the same value.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if the same value
     */
    private static boolean EQ(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == 0);
    }
    
    /**
     * Return true if <code>this</code> and <code>c</code>
     * have different values.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if the different values
     */
    private static boolean NE(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == -1) || (result == 1);
    }
    
    /**
     * Return true if the value of <code>this</code> is less than the value of <code>c</code>.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if value of this is less than the value of c.
     */
    private static boolean LT(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == -1);
    }
    
    /**
     * Return true if the value of <code>this</code> is less than or equal to the value of <code>c</code>.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if value of this is less than or equal to the value of c.
     */
    private static boolean LE(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == 0) || (result == -1);
    }
    
    /**
     * Return true if the value of <code>this</code> is greater than or equal to the value of <code>c</code>.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if value of this is greater than or equal to the value of c.
     */
    private static boolean GE(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == 0) || (result == 1);
    }
    
    /**
     * Return true if the value of <code>this</code> is greater than the value of <code>c</code>.
     * It is an error if  <code>c</code> has a different tag from <code>this</code>.
     *
     * @param c the other constant.
     * @return true if value of this is greater than the value of c.
     */
    private static boolean GT(Object obj1, Object obj2) {
        int result = compare(obj1, obj2);
        return (result == 1);
    }

}
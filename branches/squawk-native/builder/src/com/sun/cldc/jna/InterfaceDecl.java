package com.sun.cldc.jna;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A parsed out version of the Class for easy code generation
 */
class InterfaceDecl {

    Class interfaceClass;
    String[] includes;
    String library;
    ArrayList<Field> defines;
    HashSet<String> globals;
    ArrayList<Field> instanceVars;
    ArrayList<Class> structs;
    ArrayList<Method> methods;
    HashMap<String, Method> getters;
    HashMap<String, Method> setters;

    public InterfaceDecl(Class interfaceClass) throws JNAGenException {
        super();
        this.interfaceClass = interfaceClass;
        defines = new ArrayList<Field>();
        globals = new HashSet<String>();
        instanceVars = new ArrayList<Field>();
        structs = new ArrayList<Class>();
        methods = new ArrayList<Method>();
        includes = new String[0];
        getters = new HashMap<String, Method>();
        setters = new HashMap<String, Method>();
        processClass();
    }

    void checkMatchingAccessors(Method getter, Method setter) throws JNAGenException {
        if (getter != null && setter != null) {
            if (getter.getReturnType() != setter.getParameterTypes()[0]) {
                throw new JNAGenException("The signature of the getter " + getter + " != the setter " + setter);
            }    
        }        
    }
    
    void processGetter(Method m, String nativeName) throws JNAGenException {
        if (m.getParameterTypes().length != 0 || m.getReturnType() == Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        checkMatchingAccessors(m, setters.get(nativeName));
        getters.put(nativeName, m);
    }

    void processSetter(Method m, String nativeName) throws JNAGenException {
        if (m.getParameterTypes().length != 1 ||
                m.getReturnType() != Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        checkMatchingAccessors(getters.get(nativeName), m);
        setters.put(nativeName, m);
    }

    void processClass() throws JNAGenException {
        Field[] fields = interfaceClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifier = f.getModifiers();
            if (Modifier.isStatic(modifier)) {
                if (Modifier.isFinal(modifier)) {
                    try {
                        if (f.getName().equals("INCLUDES")) {
                            Object val = f.get(null);
                            if (val instanceof String[]) {
                                includes = (String[]) val;
                            } else {
                                throw new JNAGenException("The interface " + interfaceClass.getName() + " has an INCLUDES field not of type String[]");
                            }
                        } else if (f.getName().equals("LIBRARY")) {
                            Object val = f.get(null);
                            if (val instanceof String) {
                                library = (String) val;
                            } else {
                                throw new JNAGenException("The interface " + interfaceClass.getName() + " has an LIBRARY field not of type String");
                            }
                        } else {
                            defines.add(f);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("not expecting non-final static fields in interface");
                }
            } else {
                instanceVars.add(f);
            }
        }
        Method[] meths = interfaceClass.getDeclaredMethods();
        for (int i = 0; i < meths.length; i++) {
            Method m = meths[i];
            String nativeName = null;
            boolean doAccessor = false;
            boolean mayBeSetter = m.getName().indexOf("set") == 0;
            Annotation annot = m.getAnnotation(GlobalVarNamed.class);
            if (annot != null) {
                GlobalVarNamed globalVarNamed = (GlobalVarNamed) annot;
                doAccessor = true;
                nativeName = globalVarNamed.value();
            } else if (m.getAnnotation(GlobalVar.class) != null) {
                doAccessor = true;
                String javaName = m.getName();
                if (mayBeSetter) {
                    nativeName = javaName.substring(3);
                } else {
                    nativeName = javaName;
                }
            }

            if (doAccessor) {
                globals.add(nativeName);
                if (mayBeSetter) {
                    processSetter(m, nativeName);
                } else {
                    processGetter(m, nativeName);
                }
            } else {
                if (Modifier.isAbstract(m.getModifiers())) {
                    methods.add(m);
                }
            }
        }
    }
}

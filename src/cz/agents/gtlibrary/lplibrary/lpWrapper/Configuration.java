package cz.agents.gtlibrary.lplibrary.lpWrapper;

import java.io.File;
import java.util.Random;

// -Djava.library.path=/path/of/cz.agents.lplibrary.cplex/installation

public class Configuration {

    public static boolean EXPLICITLY_LOAD_CPLEX_WIN32 = true;
    public static boolean EXPLICITLY_LOAD_CPLEX_LINUX_LIBRARY = false;

    //public static final String ILOG_LICENSE_FILE = "./Libs/access.ilm";
    public static final String ILOG_LICENSE_FILE = "c:\\ILOG\\ILM\\access.ilm";

    // Default location for license should be /usr/ilog/ilm/access.ilm
    // Otherwise set EXPLICITLY_LOAD_LICENSE to true
    public static final boolean EXPLICITLY_LOAD_LICENSE = false; // License file is automatically loaded if present in /usr/ilog/ilm/access.ilm
    public static final boolean HPCC = false;

    public static final int MIP_PRESOLVE = 1;
    public static final int MIP_TIMELIMIT = -1;
    public static final double MIP_TOLERANCE = 0.001;

    public static final int MM = Integer.MAX_VALUE;

    public static final boolean FAILURE = false;
    public static final boolean SUCCESS = true;
    public static final double EPSILON = 0.00001;

    public static final long seed = System.currentTimeMillis();
    public static final Random random = new Random(seed);

    public static final boolean PRINT_ERROR = false;

    public enum NODE_TYPE {
        SOURCE, INTERMEDIATE, TARGET;

        public String toString() {
            switch (this) {
                case SOURCE:
                    return "S";
                case INTERMEDIATE:
                    return "I";
                case TARGET:
                    return "T";
                default:
                    return "";
            }
        }
    }

    ;

    public enum EDGE_TYPE {NORMAL, VIRTUAL}

    ;

    public static final boolean WARMSTARTLPS = false;
    public static final boolean TRUNCATELPS = true;

    private static boolean loaded = false;

    public static void loadLibrariesGLPK() {
        if (!loaded) {
            String osType = System.getProperty("os.arch");
            String osName = System.getProperty("os.name");
            System.out.println("Found operating system: " + osName + " on architecture " + osType + ".");
            File GLPKFile;
            File GLPKFile_Java;
            // [Zhengyu] Assume java.library.path is set to the right folder
            if (osName.toLowerCase().contains("win")) { //Found windows
                /*if(osType.contains("64")){ //64 bit version libraries should load
                        GLPKFile= new File(Configuration.GLPKLIB_WIN_64);
                        GLPKFile_Java= new File(Configuration.GLPKJAVALIB_WIN_64);
                    }else{ //32 bit probably
                        GLPKFile= new File(Configuration.GLPKLIB_WIN_32);
                        GLPKFile_Java= new File(Configuration.GLPKJAVALIB_WIN_32);
                    }*/
                GLPKFile = new File(System.getProperty("java.library.path") + "/glpk_4_44.dll");
                GLPKFile_Java = new File(System.getProperty("java.library.path") + "/glpk_4_44_java.dll");
            } else if (osName.toLowerCase().contains("linux")) {
                /*GLPKFile= new File(Configuration.GLPKLIB_LINUX);
                    GLPKFile_Java= new File(Configuration.GLPKJAVALIB_LINUX);*/
                GLPKFile = new File(System.getProperty("java.library.path") + "/libglpk.so");
                GLPKFile_Java = new File(System.getProperty("java.library.path") + "/libglpk_java.so");
            } else {//else try Mac
                /*GLPKFile= new File(Configuration.GLPKLIB_MAC);
                    GLPKFile_Java= new File(Configuration.GLPKJAVALIB_MAC);*/
                GLPKFile = new File(System.getProperty("java.library.path") + "/libglpk.dylib");
                GLPKFile_Java = new File(System.getProperty("java.library.path") + "/libglpk_java.dylib");
            }

            //Finally, load the libs.
            System.load(GLPKFile.getAbsolutePath());
            System.load(GLPKFile_Java.getAbsolutePath());
        }
        loaded = true;
    }

//	public static void loadLibrariesCPLEXWin32() {
//		File LibFile = null;
//		LibFile = new File(Configuration.CPLEXLIB_32_WIN);
//		//Finally, load the libs.
//		System.load(LibFile.getAbsolutePath());
//	}
//
//	public static void loadLibrariesCPLEXLinux() {
//		if(!loaded){
//			String osType= System.getProperty("os.arch");
//			String osName= System.getProperty("os.name");
//			System.out.println("Found operating system: "+osName+" on architecture " + osType+".");
//			File LibFile = null;
//			if (osName.toLowerCase().contains("linux")){
//				if ( Configuration.HPCC ) {
//					if(osType.contains("64")) {
//						LibFile = new File(Configuration.CPLEXLIB_64_LINUX_HPCC);
//					}
//					else {
//						// 32 bit
//						LibFile = new File(Configuration.CPLEXLIB_32_LINUX_HPCC);
//					}
//				} else if (osName.toLowerCase().contains("linux")) {
//					if(osType.contains("64")) {
//						LibFile = new File(Configuration.CPLEXLIB_64_LINUX);
//					}
//					else {
//						// 32 bit
//						LibFile = new File(Configuration.CPLEXLIB_32_LINUX);
//					}
//				} // else if load for Mac and Windows too
//				// add code here ...
//			}
//			//Finally, load the libs.
//			System.load(LibFile.getAbsolutePath());
//		}
//		loaded = true;
//	}
}

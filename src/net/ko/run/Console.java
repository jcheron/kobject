package net.ko.run;

import java.io.*; 

public class Console 
{ 

public static PrintWriter out; 

static { 
try { 
    String osName = System.getProperty("os.name"); 

    if (osName.startsWith("Windows")) { 
        out = new PrintWriter(new OutputStreamWriter(System.out, "Cp850"), true); 
    } 
    else  { 
        out = new PrintWriter(new OutputStreamWriter(System.out), true); 
    } 

} 
catch (Exception e)  { 
    out = new PrintWriter(new OutputStreamWriter(System.out), true); 
} 

} 

// -------------------------------------------------------------------- 

}
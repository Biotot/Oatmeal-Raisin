package adapters;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {

    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);

    //HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
    HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
    
    boolean WriteProcessMemory(Pointer p, long address, Pointer buffer, int size, IntByReference written);
    
    boolean ReadProcessMemory(HANDLE hProcess, int inBaseAddress, Memory outputBuffer, int nSize, IntByReference outNumberOfBytesRead);  

    boolean CloseHandle(Pointer hObject);
    
    public HANDLE GetCurrentProcess();
    
    boolean CloseHandle(HANDLE hObject);
}
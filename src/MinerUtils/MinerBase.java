package MinerUtils;
import java.util.ArrayList;
import java.util.List;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

import adapters.PsapiTools;
import adapters.Kernel32;
import adapters.Module;
import adapters.User32;

public class MinerBase {


	/*
	 * MINER BASE VARIABLES
	 */
	static int O_GameBase;
	static int m_PID;
	static HANDLE m_Game;



	/*
	 * STUPID ASS UTIL SHIT
	 */
	static Kernel32 kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);  
	static User32 user32 = (User32) Native.loadLibrary("user32", User32.class); 
	static String gameWindowName= "League of Legends (TM) Client";
	static String gameExe = "League of Legends.exe";



	public static void main(String[] args) {
		System.out.println("MinerBase Main");
		/*
		MinerBase.Setup();
		Unit aUnit = MinerBase.GetPlayer(0);
		ArrayList<Unit> aChampList = new ArrayList<Unit>();
		for (int x=0; x<10; x++)
		{
			aChampList.add(MinerBase.GetPlayer(x));
		}
		float[] aScreenCoord = MinerBase.GetScreenCoords();
		*/
	}

	public static void Setup()
	{
		enableDebugPrivilege(); //May be unneeded

		m_PID = getProcessId(gameWindowName);
		System.out.println("Pid = " + m_PID); 

		O_GameBase = findBaseAddress(m_PID); 

		System.out.println("Base Address: " + Integer.toHexString((int) O_GameBase));
		m_Game = openProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, m_PID);

	}


	/**
	 * Finds and returns the base memory address of a given program ID
	 */
	private static int findBaseAddress(int pID) {
		try {
			HANDLE game = openProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, pID);
			List<Module> modules = PsapiTools.getInstance().EnumProcessModulesEx(game, 0x01);
			for (Module module : modules) {
				if(module.getBaseName().equals(gameExe)){		
					if(module.getLpBaseOfDll() != null){
						int baseAddress = ((Long)Pointer.nativeValue(module.getLpBaseOfDll().getPointer())).intValue();
						return baseAddress;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error finding base address");
			return -1;
		}
		return 0;
	}


	/**
	 * Reads the memory at a given address
	 */
	public static Memory readMemory(HANDLE process, int address, int bytesToRead) {  
		IntByReference read = new IntByReference(0);  
		Memory output = new Memory(4); 
		kernel32.ReadProcessMemory(process, address, output, bytesToRead, read);  
		return output;  
	} 


	/**
	 * Opens a process given the pID and given permissions
	 */
	public static HANDLE openProcess(int permissions, int pID) {  
		HANDLE process = kernel32.OpenProcess(permissions, true, pID);  
		return process;  
	}  

	/**
	 * Returns the process ID of a given window
	 */
	public static int getProcessId(String window) {  
		IntByReference pid = new IntByReference(0);  
		user32.GetWindowThreadProcessId(user32.FindWindowA(null, window), pid);  
		return pid.getValue();  
	}  

	/**
	 * Should fix "Insufficient Access" errors, may be unneeded
	 */
	private static void enableDebugPrivilege() {
		HANDLEByReference hToken = new HANDLEByReference();
		boolean success = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
				WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES, hToken);
		if (!success) {
			System.out.println("OpenProcessToken failed. Error: {}" + Native.getLastError());
			return;
		}
		WinNT.LUID luid = new WinNT.LUID();
		success = Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid);
		if (!success) {
			System.out.println("LookupprivilegeValue failed. Error: {}" + Native.getLastError());
			return;
		}
		WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
		tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
		success = Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
		if (!success) {
			System.out.println("AdjustTokenPrivileges failed. Error: {}" + Native.getLastError());
		}
		Kernel32.INSTANCE.CloseHandle(hToken.getValue());
	}
}

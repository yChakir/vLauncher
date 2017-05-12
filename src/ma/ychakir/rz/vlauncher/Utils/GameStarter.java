package ma.ychakir.rz.vlauncher.Utils;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yassine
 */
public class GameStarter {
    private ProcessBuilder game;

    public GameStarter(String sframe, String args) {
        WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;
        sa.lpSecurityDescriptor = null;
        sa.dwLength = new WinDef.DWORD(sa.size());

        WinNT.HANDLE handle = Kernel32.INSTANCE.CreateEvent(sa, false, false, null);

        List<String> cmd = new ArrayList<>();
        cmd.add(sframe);
        cmd.addAll(Arrays.asList(args.split(" ")));

        game = new ProcessBuilder(cmd);
        game.environment().put(sframe + "_PARENT", "Launcher.exe");
        game.environment().put(sframe + "_RUNNER", String.valueOf(Long.parseUnsignedLong(handle.getPointer().toString().substring(9), 16)));
    }

    public boolean start() throws IOException {
        Process p = game.start();
        return p.isAlive();
    }
}

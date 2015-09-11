import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by connor on 06/09/15.
 */
public class WindowsCommandSet extends CommandSet
{
//    @Override
//    public void beep()
//    {
//        getRuntime().exec("")
//    }

    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("eject");
    }

    @Override
    public void shutdown() throws IOException
    {
        getRuntime().exec("shutdown -s");
    }

    @Override
    public void restart() throws IOException
    {
        getRuntime().exec("shutdown -r");
    }

    @Override
    public void takeCameraPicture()
    {

    }

    @Override
    public void setWallpaper(Image newWallpaper)
    {

    }
}

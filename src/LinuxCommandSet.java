import java.awt.*;
import java.io.IOException;

public class LinuxCommandSet extends CommandSet
{
    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("eject");
    }

    @Override
    public void playSound()
    {

    }

    @Override
    public void shutdown()
    {

    }

    @Override
    public void restart()
    {

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
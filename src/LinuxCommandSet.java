import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LinuxCommandSet extends CommandSet
{
    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("eject");
    }

    @Override
    public void shutdown() throws IOException
    {

    }

    @Override
    public void restart() throws IOException
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
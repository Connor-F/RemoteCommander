import java.awt.*;
import java.io.IOException;

/**
 * Created by connor on 06/09/15.
 */
public class MacCommandSet extends CommandSet
{
    @Override
    public void eject() throws IOException
    {
        getRuntime().exec("drutil eject internal");
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

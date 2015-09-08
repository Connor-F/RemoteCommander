import java.util.ArrayList;
import java.util.Random;

/**
 * holds valid key codes for the chaos command
 */
public class KeyCode
{
    private ArrayList<Integer> keys;

    public KeyCode()
    {
        keys = populateKeyCodes();
    }

    /**
     * uses valid keycodes to populate our keycode arraylist
     * @return an arraylist of integers that contain valid keycodes
     */
    private ArrayList<Integer> populateKeyCodes()
    {
        ArrayList<Integer> keycodes = new ArrayList<>();
        keycodes.add(new Integer(8));
        keycodes.add(new Integer(9));
        keycodes.add(new Integer(13));
        keycodes.add(new Integer(27));
        keycodes.add(new Integer(45));
        keycodes.add(new Integer(46));
        keycodes.add(new Integer(144));
        keycodes.add(new Integer(145));
        for(int i = 16; i <= 20; i++)
            keycodes.add(new Integer(Integer.valueOf(i)));
        for(int i = 33; i <= 40; i++)
            keycodes.add(new Integer(Integer.valueOf(i)));
        for(int i = 48; i <= 57; i++)
            keycodes.add(new Integer(Integer.valueOf(i)));
        for(int i = 65; i <= 123; i++)
            keycodes.add(new Integer(Integer.valueOf(i)));

        return keycodes;
    }

    /**
     * gets a random valid keycode to be used with the chaos command
     * @return a random keycode
     */
    public int randomKeyCode()
    {
        return keys.get(new Random().nextInt(keys.size()));
    }
}

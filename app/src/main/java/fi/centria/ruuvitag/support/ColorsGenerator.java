package fi.centria.ruuvitag.support;

import android.graphics.Color;

import java.util.Random;

public class ColorsGenerator
{

    static int[] barColors = {
            Color.rgb(93,138,168),
            Color.rgb(227,38,54),
            Color.rgb(255,191,0 ),
            Color.rgb(164,198,57),
            Color.rgb(205,149,117),
            Color.rgb(0 ,128,0 ),
            Color.rgb(0,255,255),
            Color.rgb(127,255,212),
            Color.rgb(75,83,32),
            Color.rgb(233,214,107 ),
            Color.rgb(178,190,181),
            Color.rgb(165,42,420),
            Color.rgb(253,238,0),
            Color.rgb(0,127,255),
            Color.rgb(132,132,130),
            Color.rgb(255,225,53),
            Color.rgb(0,0,0),
            Color.rgb(254,111,94),
            Color.rgb(135,50,96),
            Color.rgb(181,166,66),
            Color.rgb(102 ,255,0),
            Color.rgb(0,66,37),
            Color.rgb(205,127,50),
            Color.rgb(165,42,42),
            Color.rgb(204,85,0),
            Color.rgb(0,204,153),
    };

    public static int getColor(int index)
    {
        if(index < barColors.length-1)
        {
            return barColors[index];
        }else
        {
            Random random = new Random();
            return Color.rgb(random.nextInt(255),random.nextInt(255),random.nextInt(255));
        }


    }
}

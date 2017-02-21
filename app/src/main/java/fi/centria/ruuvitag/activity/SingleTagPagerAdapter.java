package fi.centria.ruuvitag.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import fi.centria.ruuvitag.data.RuuvitagObject;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by ISOHAJA on 1.2.2017.
 */

class SingleTagPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter
{
    public static final int AIR_PRESSURE = 1;
    public static final int RSSI = 2;
    public static final int TEMPERATURE = 3;
    public static final int HUMIDITY = 4;
    private final Context context;
    public static RuuvitagObject tagToMonitor;
    SingleTagDataView currentFragment;
    private int index;
    UpdatableFragment mCurrentFragment;

    public SingleTagPagerAdapter(Context c, FragmentManager fm) {

        super(fm);

        this.context = c;
    }

    @Override
    public Fragment getItem(int position)
    {
        SingleTagDataView currentFragment =  SingleTagDataView.newInstance(RSSI);

        if(position == 0)
            currentFragment.setType(RSSI);
        else if(position == 1)
            currentFragment.setType(TEMPERATURE);
        else if(position == 2)
            currentFragment.setType(HUMIDITY);
        else if(position == 3)
            currentFragment.setType(AIR_PRESSURE);

        currentFragment.update(tagToMonitor);
        return currentFragment;

    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = (UpdatableFragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }


    @Override
    public int getItemPosition(Object object)
    {
        if (object instanceof UpdatableFragment)
        {
            ((UpdatableFragment) object).update(this.getBeacon());
        }
        //don't return POSITION_NONE, avoid fragment recreation.
        return POSITION_NONE;// super.getItemPosition(object);
    }

    @Override
    public int getCount()
    {
        return 4;
    }

    public void setBeacon(RuuvitagObject beacon)
    {

        this.tagToMonitor = beacon;
        if(mCurrentFragment != null)
        mCurrentFragment.update(tagToMonitor);



    }

    public RuuvitagObject getBeacon(){

        return tagToMonitor;
    }

    public void setBeaconMissing() {
        mCurrentFragment.update(null);
    }
}
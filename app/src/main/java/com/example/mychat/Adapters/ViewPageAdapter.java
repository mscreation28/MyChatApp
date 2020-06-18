package com.example.mychat.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.mychat.Fragment.ChatFrag;
import com.example.mychat.Fragment.FriendsFrag;
import com.example.mychat.Fragment.RequestFrag;

public class ViewPageAdapter extends FragmentPagerAdapter {

    public ViewPageAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFrag requestFrag = new RequestFrag();
                return requestFrag;
            case 1:
                ChatFrag chatFrag = new ChatFrag();
                return chatFrag;
            case 2:
                FriendsFrag friendsFrag = new FriendsFrag();
                return friendsFrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}

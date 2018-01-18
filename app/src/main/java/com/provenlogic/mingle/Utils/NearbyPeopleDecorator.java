package com.provenlogic.mingle.Utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Anurag on 17/4/17.
 */

public class NearbyPeopleDecorator extends RecyclerView.ItemDecoration{

    private int space;
    /**
     *
     * @param space
     */
    public NearbyPeopleDecorator(int space){
       this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //outRect.left = space;
        //outRect.right = space;
        //outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        int currentPos = parent.getChildAdapterPosition(view);
        if( (currentPos - 1) % 3 == 0){
            if(parent.getChildAdapterPosition(view) == 1){
                outRect.top = space;
            }
        }else if(currentPos > 2){
            outRect.top = (-1)*space;
        }
    }
}

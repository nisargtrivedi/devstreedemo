package com.devstree.project.listener;

import com.devstree.project.model.Place;

/**
 * Created by Jitendra on 23,November,2022
 */
public interface ItemClickListener {
    void itemClicked(Place place, int position, String action);
}

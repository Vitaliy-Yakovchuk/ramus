package com.ramussoft.pb.print.web;

import java.util.ArrayList;
import java.util.List;

public class History {

    private List<String> locations = new ArrayList<String>();

    private int position = -1;

    public void update(String location) {
        if ((locations.size() == 0)
                || (!locations.get(position).equals(location))) {
            addLocation(location);
        }
    }

    private void addLocation(String location) {
        if (position + 1 < locations.size()) {
            locations = locations.subList(0, position + 1);
        }
        locations.add(location);
        position++;
    }

    public boolean canGonBack() {
        return position > 0;
    }

    public boolean canGoForward() {
        return position + 1 < locations.size();
    }

    public String goBack() {
        position--;
        return locations.get(position);
    }

    public String goForward() {
        position++;
        return locations.get(position);
    }
}

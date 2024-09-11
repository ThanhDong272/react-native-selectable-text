package com.rob117.selectabletext;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ActionMode;
import android.view.ActionMode.Callback;

import java.util.Map;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.text.ReactTextView;
import com.facebook.react.views.text.ReactTextViewManager;

import java.util.List;
import java.util.ArrayList;

public class RNSelectableTextManager extends ReactTextViewManager {

    private static final String REACT_CLASS = "RNSelectableText";
    private boolean isExtendedMenuShown = false; // Flag to track if extended menu is shown

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public ReactTextView createViewInstance(ThemedReactContext context) {
        return new ReactTextView(context);
    }

    @ReactProp(name = "menuItems")
    public void setMenuItems(ReactTextView textView, ReadableArray items) {
        List<String> result = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            result.add(items.getString(i));
        }
        registerSelectionListener(result.toArray(new String[0]), textView);
    }

    @ReactProp(name = "menuItemsExtend")
    public void setMenuItemsExtend(ReactTextView textView, ReadableArray itemsExtend) {
        if (itemsExtend != null) {
            List<String> resultExtend = new ArrayList<>(itemsExtend.size());
            for (int i = 0; i < itemsExtend.size(); i++) {
                resultExtend.add(itemsExtend.getString(i));
            }
            textView.setTag(resultExtend); // Store extended menu items in the view tag
        }
    }

    private void registerSelectionListener(final String[] menuItems, final ReactTextView view) {
        view.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.clear();

                if (!isExtendedMenuShown) {
                    for (int i = 0; i < menuItems.length; i++) {
                        menu.add(0, i, 0, menuItems[i]);
                    }
                } else {
                    List<String> extendedItems = (List<String>) view.getTag();
                    if (extendedItems != null) {
                        for (int i = 0; i < extendedItems.size(); i++) {
                            menu.add(0, i, 0, extendedItems.get(i));
                        }
                    }
                }

                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isExtendedMenuShown = false; // Reset the flag when action mode is destroyed
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Get selected text range
                int selectionStart = view.getSelectionStart();
                int selectionEnd = view.getSelectionEnd();
                String selectedText = view.getText().toString().substring(selectionStart, selectionEnd);

                // If the first item in menuItems is selected, toggle to menuItemsExtend
                if (item.getItemId() == 0 && !isExtendedMenuShown) {
                    List<String> extendedItems = (List<String>) view.getTag();
                    if (extendedItems != null) {
                        isExtendedMenuShown = true; // Set the flag to show the extended menu
                        mode.invalidate(); // Invalidate the action mode to reload the menu
                        return true; // Do not end the action mode here
                    }
                }

                if (isExtendedMenuShown) {
                    // Handle selection from extended menu
                    List<String> extendedItems = (List<String>) view.getTag();
                    if (extendedItems != null && item.getItemId() < extendedItems.size()) {
                        onSelectNativeEvent(view, extendedItems.get(item.getItemId()), selectedText, selectionStart, selectionEnd);
                    }
                } else {
                    onSelectNativeEvent(view, menuItems[item.getItemId()], selectedText, selectionStart, selectionEnd);
                }

                // End action mode and reset the flag after selecting an item
                isExtendedMenuShown = false;
                mode.finish();
                return true;
            }
        });
    }

    private void onSelectNativeEvent(ReactTextView view, String eventType, String content, int selectionStart, int selectionEnd) {
        WritableMap event = Arguments.createMap();
        event.putString("eventType", eventType);
        event.putString("content", content);
        event.putInt("selectionStart", selectionStart);
        event.putInt("selectionEnd", selectionEnd);

        // Dispatch event to JS
        ReactContext reactContext = (ReactContext) view.getContext();
        reactContext
            .getJSModule(RCTEventEmitter.class)
            .receiveEvent(view.getId(), "topSelection", event);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.builder().put("topSelection",MapBuilder.of("registrationName","onSelection")).build();
    }
}

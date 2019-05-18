package com.record.recordingapp.dummy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

  /**
   * An array of sample (dummy) items.
   */
  public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();
  public static final List<JSONObject> objects = new ArrayList<JSONObject>();

  /**
   * A dummy item representing a piece of content.
   */
  public static class DummyItem {
    public final int id;
    public final String phone;
    public final String content;
    public final String contentUrl;

    public DummyItem(int id, String phone, String content, String contentUrl) {
      this.id = id;
      this.content = content;
      this.phone = phone;
      this.contentUrl = contentUrl;
    }
  }
}

package com.example.demotestspringautoconfig.util;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TreeItem {
  private final String name;
  private final List<TreeItem> children;
  private final List<String> values;

  public TreeItem(@NonNull String name) {
    this.name = name;
    children = new ArrayList<>(2);
    values = new ArrayList<>(2);
  }

  @NonNull
  @SuppressWarnings("null")
  public String getName() {
    return name;
  }

  @NonNull
  @SuppressWarnings("null")
  public List<TreeItem> getChildren() {
    return children;
  }

  @NonNull
  @SuppressWarnings("null")
  public List<String> getValues() {
    return values;
  }

  @NonNull
  @SuppressWarnings("null")
  public TreeItem getOrCreateChild(String name) {
    for (TreeItem child : getChildren()) {
      if (child.getName().equals(name))
        return child;
    }
    final TreeItem created = new TreeItem(name);
    getChildren().add(created);
    return created;
  }

  public void addValue(String value) {
    getValues().add(value);
  }

  public void sort() {
    Collections.sort(values);
    children.sort(new TreeItemComparator());
    children.forEach(TreeItem::sort);
  }

  protected static class TreeItemComparator implements Comparator<TreeItem> {

    @Override
    public int compare(TreeItem o1, TreeItem o2) {
      final String n1 = o1.getName();
      final String n2 = o2.getName();
      return n1.compareTo(n2);
    }
  }
}
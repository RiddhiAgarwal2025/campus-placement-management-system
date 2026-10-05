package com.campusplacement.ui.components;

/** Implemented by pages that support live search and filtering. */
public interface Searchable {
    /** Sets the search query and applies the filter to the page. */
    void setSearch(String query);

    /** Returns the current search query text. */
    default String getSearch() {
        return "";
    }
}

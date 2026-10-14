package me.alex.tags.gui;

public record PageLayout(int page, int totalPages, int start, int end) {
    public static final int TAGS_PER_PAGE = 45;
    public static final int SIZE = 54;
    public static final int PREVIOUS_SLOT = 45;
    public static final int NEXT_SLOT = 53;

    public static PageLayout of(int count, int requestedPage) {
        if (count < 0) {
            throw new IllegalArgumentException("Tag count cannot be negative.");
        }
        int total = Math.max(1, (int) ((count + (long) TAGS_PER_PAGE - 1) / TAGS_PER_PAGE));
        int page = Math.max(0, Math.min(requestedPage, total - 1));
        int start = page * TAGS_PER_PAGE;
        return new PageLayout(page, total, start, (int) Math.min((long) start + TAGS_PER_PAGE, count));
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }
}

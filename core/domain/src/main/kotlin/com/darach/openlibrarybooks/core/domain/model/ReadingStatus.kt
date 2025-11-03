package com.darach.openlibrarybooks.core.domain.model

/**
 * Represents the reading status of a book in the user's library.
 *
 * Maps to Open Library's reading list shelves:
 * - WantToRead: "want-to-read" shelf
 * - CurrentlyReading: "currently-reading" shelf
 * - AlreadyRead: "already-read" shelf
 */
enum class ReadingStatus {
    /**
     * Books the user wants to read in the future
     */
    WantToRead,

    /**
     * Books the user is currently reading
     */
    CurrentlyReading,

    /**
     * Books the user has finished reading
     */
    AlreadyRead,
}

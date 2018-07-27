package org.checklist.comics.comicschecklist.service

/**
 * Custom class used to send message from service to UI.
 *
 * This class is only composed by basic data, but it could have more complex one, like [android.os.Bundle].
 */
data class Message(val result: Int, val editor: String)

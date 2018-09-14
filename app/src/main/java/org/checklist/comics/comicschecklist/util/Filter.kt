package org.checklist.comics.comicschecklist.util

data class Filter(val sections: Constants.Sections, val textToSearch: String) {
    constructor(sections: Constants.Sections) : this(sections, "")
}
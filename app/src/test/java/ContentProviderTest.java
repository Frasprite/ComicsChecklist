import android.test.ProviderTestCase2;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.util.DateCreator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ContentProviderTest extends ProviderTestCase2<ComicContentProvider> {

    /**
     * Constructor.
     *
     * @param providerClass     The class name of the provider under test
     * @param providerAuthority The provider's authority string
     */
    public ContentProviderTest(Class<ComicContentProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Test
    public void testInsertData() {
        // TODO complete test
        /*assertTrue("Insertion failed!",
                ComicDatabaseManager.insert(
                        mMockContext,
                        "Sport Stars 4",
                        "da comprare",
                        "N.D.",
                        "29/06/2016",
                        DateCreator.elaborateDate("29/06/2016"),
                        "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                        "N.D.",
                        "Free",
                        "yes",
                        "no",
                        "http://digitalcomicmuseum.com/") > 0);*/
    }
}

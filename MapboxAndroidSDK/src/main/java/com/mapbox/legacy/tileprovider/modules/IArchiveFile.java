package com.mapbox.legacy.tileprovider.modules;

import com.mapbox.legacy.tileprovider.MapTile;
import com.mapbox.legacy.tileprovider.tilesource.ITileLayer;
import java.io.InputStream;

public interface IArchiveFile {

    /**
     * Get the input stream for the requested tile.
     *
     * @return the input stream, or null if the archive doesn't contain an entry for the requested
     * tile
     */
    InputStream getInputStream(ITileLayer tileSource, MapTile tile);
}

package HereMaps;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * Created by Usuario on 21/02/2016.
 */
public class HereTileSource extends OnlineTileSourceBase {

    public HereTileSource(final String aName, int aResourceId, int aZoomMinLevel, int aZoomMaxLevel, String fileEnding, final String[] aBaseUrl) {
        super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, fileEnding, aBaseUrl);
    }

    @Override
    public String getTileURLString(final MapTile aTile) {
        return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + "/"+256+"/png8?app_id=eZEV87jQ6kI85OngHNd2&app_code=IdrTiN7Fw5il_1zqRZiLhQ";
    }



}

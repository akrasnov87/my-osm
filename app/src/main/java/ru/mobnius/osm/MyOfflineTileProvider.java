package ru.mobnius.osm;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MyOfflineTileProvider extends MapTileProviderArray implements IMapTileProviderCallback {

    private IArchiveFile[] archives;
    /**
     * Creates a {@link MapTileProviderBasic}.
     * throws with the source[] is null or empty
     */
    public MyOfflineTileProvider(final IRegisterReceiver pRegisterReceiver, File[] source) {
        super(FileBasedTileSource.getSource(source[0].getName()), pRegisterReceiver);
        List<IArchiveFile> files = new ArrayList<>();

        for (final File file : source){
            final IArchiveFile temp= ArchiveFileFactory.getArchiveFile(file);
            if (temp!=null)
                files.add(temp);
            else{
                Log.w(IMapView.LOGTAG, "Skipping " + file + ", no tile provider is registered to handle the file extension");
            }
        }
        archives = new IArchiveFile[files.size()];
        archives=files.toArray(archives);
        final MapTileFileArchiveProvider mapTileFileArchiveProvider = new MapTileFileArchiveProvider(pRegisterReceiver, getTileSource(), archives);
        mTileProviderList.add(mapTileFileArchiveProvider);

        final ITileSource tileSource = new XYTileSource( "HOT", 1, 20, 256, ".png",
                new String[] {
                        "http://cic.it-serv.ru/osm/" },"© OpenStreetMap contributors");

        MapTileDownloader mapTileDownloader = new MapTileDownloader(tileSource);
        this.mTileProviderList.add(mapTileDownloader);

        final MapTileApproximater approximationProvider = new MapTileApproximater();
        mTileProviderList.add(approximationProvider);
        approximationProvider.addProvider(mapTileFileArchiveProvider);

    }

    public IArchiveFile[] getArchives(){
        return archives;
    }

    public void detach() {
        if (archives!=null){
            for (final IArchiveFile file : archives){
                file.close();
            }
        }
        super.detach();
    }

    @Override
    protected boolean isDowngradedMode(final long pMapTileIndex) {
        return true;
    }
}

package com.example.ravneet.cameratester;

/**
 * Created by Ravneet on 1/17/16.
 */

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.InputStream;
import java.util.logging.Logger;


public class DriveIdDataFetcher implements DataFetcher<InputStream> {
    private static final Logger LOG = Logger.getLogger(DriveIdDataFetcher.class.getName());

    private final GoogleApiClient client;
    private final DriveId driveId;

    private boolean cancelled = false;


    private DriveFile file;
    private DriveContents contents;

    public DriveIdDataFetcher(GoogleApiClient client, DriveId driveId) {
        this.client = client;
        this.driveId = driveId;
    }

    public String getId() {
        return driveId.encodeToString();
    }

    public InputStream loadData(Priority priority) {
        if (cancelled) return null;
        if (client == null) {
            LOG.warning("No connected client received, giving custom error image");
            return null;
        }
        file = Drive.DriveApi.getFile(client, driveId);
        if (cancelled) return null;
        contents = sync(file.open(client, DriveFile.MODE_READ_ONLY, null)).getDriveContents();
        if (cancelled) return null;
        return contents.getInputStream();
    }

    public void cancel() {
        cancelled = true;
        if (contents != null) {
            contents.discard(client);
        }
    }

    public void cleanup() {
        if (contents != null) {
            contents.discard(client);
        }
    }

    private static <T extends Result> void assertSuccess(T result) {
        if (!result.getStatus().isSuccess()) {
            throw new IllegalStateException(result.getStatus().toString());
        }
    }

    private static <T extends Result> T sync(PendingResult<T> pending) {
        T result = pending.await();
        assertSuccess(result);
        return result;
    }
}
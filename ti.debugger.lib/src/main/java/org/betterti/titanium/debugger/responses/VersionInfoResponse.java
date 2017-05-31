package org.betterti.titanium.debugger.responses;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class VersionInfoResponse extends DebugResponse {
    private final String _versionName;

    public VersionInfoResponse(Long id, String versionName) {
        super(id);
        _versionName = versionName;
    }

    public String getVersionName() {
        return _versionName;
    }
}

package com.jmzsoft.jmzxmr;

import java.text.DecimalFormat;

public class Rig {

    String rigName;
    String workerId;
    String diffCurrent;
    String currentHash;
    String minerVersion;
    String uptime;
    int position;
    Pool pool;

    public int getPosition() { return position; }

    public String getRigName() {
        return rigName;
    }

    public String getDiffCurrent() {
        return diffCurrent;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public String getMinerVersion() {
        return minerVersion;
    }

    public String getUptime() {
        return uptime;
    }

    public Pool getPool() { return pool; }

    public void setPool(Pool _pool) { pool = _pool; }

    Rig(String _rigName, String _workerId, String _diffCurrent, double _currentHash, String _minerVersion, String _uptime, int _position){
        this.rigName = _rigName;
        this.workerId = _workerId;
        this.diffCurrent = _diffCurrent;
        this.currentHash = new DecimalFormat("#.#####").format(_currentHash) + " KH/s";
        this.minerVersion = _minerVersion;
        this.uptime = _uptime;
        this.position = _position;
    }
}

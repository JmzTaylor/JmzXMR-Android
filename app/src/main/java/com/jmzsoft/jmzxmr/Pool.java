package com.jmzsoft.jmzxmr;

import java.text.DecimalFormat;

public class Pool {
    public String totalHashes;
    public String validShares;
    public String amtPaid;
    public String amtDue;

    public String getTotalHashes() {
        return totalHashes;
    }

    public String getValidShares() {
        return validShares;
    }

    public String getAmtPaid() {
        return amtPaid;
    }

    public String getAmtDue() {
        return amtDue;
    }

    Pool(String _totalHashes, String _validShares, double _amtPaid, double _amtDue) {
        this.totalHashes = _totalHashes;
        this.validShares = _validShares;
        this.amtPaid = new DecimalFormat("#.#####").format(_amtPaid) + " ɱ";
        this.amtDue = new DecimalFormat("#.#####").format(_amtDue) + " ɱ";
    }
}

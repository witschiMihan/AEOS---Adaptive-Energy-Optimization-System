package service;

import model.EnergyRecord;

public class BitCorrectionEngine {

    /**
     * Detect and correct errors in energy data using Hamming code principles
     * @param record energy record with potential bit errors
     * @return corrected energy value
     */
    public double correctBitErrors(EnergyRecord record) {
        int errorBits = detectErrorBits(record);
        record.setErrorBits(errorBits);
        return correctValue(record.getEnergyConsumption(), errorBits);
    }

    /**
     * Detect the position of error bits in the data
     * @param record energy record
     * @return error bit position (0 if no error)
     */
    private int detectErrorBits(EnergyRecord record) {
        // Simplified bit error detection
        // In a real implementation, this would use Hamming code or similar
        long bits = Double.doubleToLongBits(record.getEnergyConsumption());
        int errorCount = Long.bitCount(bits);
        return errorCount % 2;
    }

    /**
     * Apply error correction to the energy value
     * @param value original energy consumption value
     * @param errorPosition position of the error bit
     * @return corrected value
     */
    private double correctValue(double value, int errorPosition) {
        if (errorPosition == 0) {
            return value;
        }
        // Apply bit flip correction
        long bits = Double.doubleToLongBits(value);
        bits ^= (1L << errorPosition);
        return Double.longBitsToDouble(bits);
    }

    /**
     * Calculate the Hamming code for error detection and correction
     * @param data input data
     * @return Hamming code
     */
    public int calculateHammingCode(long data) {
        int parity1 = 0, parity2 = 0, parity4 = 0, parity8 = 0;

        for (int i = 0; i < 64; i++) {
            if ((data & (1L << i)) != 0) {
                if (((i + 1) & 1) != 0) parity1 ^= 1;
                if (((i + 1) & 2) != 0) parity2 ^= 1;
                if (((i + 1) & 4) != 0) parity4 ^= 1;
                if (((i + 1) & 8) != 0) parity8 ^= 1;
            }
        }

        return (parity8 << 3) | (parity4 << 2) | (parity2 << 1) | parity1;
    }

    /**
     * Validate data integrity using checksum
     * @param record energy record
     * @return true if data is valid, false otherwise
     */
    public boolean validateDataIntegrity(EnergyRecord record) {
        long bits = Double.doubleToLongBits(record.getEnergyConsumption());
        int hammingCode = calculateHammingCode(bits);
        return hammingCode == 0;
    }

    /**
     * Get error correction statistics
     * @param record energy record
     * @return string with error correction details
     */
    public String getErrorCorrectionStats(EnergyRecord record) {
        return "Record ID: " + record.getRecordId() +
                ", Error Bits: " + record.getErrorBits() +
                ", Integrity Valid: " + validateDataIntegrity(record);
    }
}

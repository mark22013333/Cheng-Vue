package com.cheng.common.utils.uuid;

import com.cheng.common.exception.UtilException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 提供共用唯一識別碼（universally unique identifier）（UUID）實現
 *
 * @author cheng
 */
public final class UUID implements java.io.Serializable, Comparable<UUID> {
    private static final long serialVersionUID = -1185015143654744140L;

    /**
     * 此UUID的最高64有效位
     */
    private final long mostSigBits;
    /**
     * 此UUID的最低64有效位
     */
    private final long leastSigBits;

    /**
     * 私有建構
     *
     * @param data 數據
     */
    private UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * 使用指定的數據建構新的 UUID。
     *
     * @param mostSigBits  用於 {@code UUID} 的最高有效 64 位
     * @param leastSigBits 用於 {@code UUID} 的最低有效 64 位
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * 取得類型 4（偽隨機產生的）UUID 的靜態工廠。
     *
     * @return 隨機產生的 {@code UUID}
     */
    public static UUID fastUUID() {
        return randomUUID(false);
    }

    /**
     * 取得類型 4（偽隨機產生的）UUID 的靜態工廠。 使用加密的强偽隨機數產生器產生該 UUID。
     *
     * @return 隨機產生的 {@code UUID}
     */
    public static UUID randomUUID() {
        return randomUUID(true);
    }

    /**
     * 取得類型 4（偽隨機產生的）UUID 的靜態工廠。 使用加密的强偽隨機數產生器產生該 UUID。
     *
     * @param isSecure 是否使用{@link SecureRandom}如果是可以取得更安全的隨機碼，否則可以得到更好的效能
     * @return 隨機產生的 {@code UUID}
     */
    public static UUID randomUUID(boolean isSecure) {
        final Random ng = isSecure ? Holder.numberGenerator : getRandom();

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(randomBytes);
    }

    /**
     * 根據指定的位元組陣列取得類型 3（基於名稱的）UUID 的靜態工廠。
     *
     * @param name 用於建構 UUID 的位元組陣列。
     * @return 根據指定陣列產生的 {@code UUID}
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported");
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f; /* clear version */
        md5Bytes[6] |= 0x30; /* set to version 3 */
        md5Bytes[8] &= 0x3f; /* clear variant */
        md5Bytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(md5Bytes);
    }

    /**
     * 根據 {@link #toString()} 方法中描述的字串標準表示形式建立{@code UUID}。
     *
     * @param name 指定 {@code UUID} 字串
     * @return 具有指定值的 {@code UUID}
     * @throws IllegalArgumentException 如果 name 與 {@link #toString} 中描述的字串表示形式不符抛出此異常
     *
     */
    public static UUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * 返回指定數字對應的hex值
     *
     * @param val    值
     * @param digits 位
     * @return 值
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * 取得{@link SecureRandom}，類提供加密的强隨機數產生器 (RNG)
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException(e);
        }
    }

    /**
     * 取得隨機數產生器物件<br>
     * ThreadLocalRandom是JDK 7之後提供並發產生隨機數，能夠解決多個執行緒發生的競爭爭奪。
     *
     * @return {@link ThreadLocalRandom}
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 返回此 UUID 的 128 位值中的最低有效 64 位。
     *
     * @return 此 UUID 的 128 位值中的最低有效 64 位。
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * 返回此 UUID 的 128 位值中的最高有效 64 位。
     *
     * @return 此 UUID 的 128 位值中最高有效 64 位。
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * 與此 {@code UUID} 相關聯的版本號. 版本號描述此 {@code UUID} 是如何產生的。
     * <p>
     * 版本號具有以下含意:
     * <ul>
     * <li>1 基於時間的 UUID
     * <li>2 DCE 安全 UUID
     * <li>3 基於名稱的 UUID
     * <li>4 隨機產生的 UUID
     * </ul>
     *
     * @return 此 {@code UUID} 的版本號
     */
    public int version() {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (int) ((mostSigBits >> 12) & 0x0f);
    }

    /**
     * 與此 {@code UUID} 相關聯的變體號。變體號描述 {@code UUID} 的介面。
     * <p>
     * 變體號具有以下含意：
     * <ul>
     * <li>0 為 NCS 向後兼容保留
     * <li>2 <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>(Leach-Salz), 用於此類
     * <li>6 保留，微軟向後兼容
     * <li>7 保留供以後定義使用
     * </ul>
     *
     * @return 此 {@code UUID} 相關聯的變體號
     */
    public int variant() {
        // This field is composed of a varying number of bits.
        // 0 - - Reserved for NCS backward compatibility
        // 1 0 - The IETF aka Leach-Salz variant (used by this class)
        // 1 1 0 Reserved, Microsoft backward compatibility
        // 1 1 1 Reserved for future definition.
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
    }

    /**
     * 與此 UUID 相關聯的時間戳值。
     *
     * <p>
     * 60 位的時間戳值根據此 {@code UUID} 的 time_low、time_mid 和 time_hi 欄位建構。<br>
     * 所得到的時間戳以 100 毫微秒為單位，從 UTC（共用協調時間） 1582 年 10 月 15 日零時開始。
     *
     * <p>
     * 時間戳值僅在在基於時間的 UUID（其 version 類型為 1）中才有意義。<br>
     * 如果此 {@code UUID} 不是基於時間的 UUID，則此方法抛出 UnsupportedOperationException。
     *
     * @throws UnsupportedOperationException 如果此 {@code UUID} 不是 version 為 1 的 UUID。
     */
    public long timestamp() throws UnsupportedOperationException {
        checkTimeBase();
        return (mostSigBits & 0x0FFFL) << 48//
                | ((mostSigBits >> 16) & 0x0FFFFL) << 32//
                | mostSigBits >>> 32;
    }

    /**
     * 與此 UUID 相關聯的時鐘序列值。
     *
     * <p>
     * 14 位的時鐘序列值根據此 UUID 的 clock_seq 欄位建構。clock_seq 欄位用於保證在基於時間的 UUID 中的時間唯一性。
     * <p>
     * {@code clockSequence} 值僅在基於時間的 UUID（其 version 類型為 1）中才有意義。 如果此 UUID 不是基於時間的 UUID，則此方法抛出
     * UnsupportedOperationException。
     *
     * @return 此 {@code UUID} 的時鐘序列
     * @throws UnsupportedOperationException 如果此 UUID 的 version 不為 1
     */
    public int clockSequence() throws UnsupportedOperationException {
        checkTimeBase();
        return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * 與此 UUID 相關的節點值。
     *
     * <p>
     * 48 位的節點值根據此 UUID 的 node 欄位建構。此欄位旨在用於儲存機器的 IEEE 802 地址，該地址用於產生此 UUID 以保證空間唯一性。
     * <p>
     * 節點值僅在基於時間的 UUID（其 version 類型為 1）中才有意義。<br>
     * 如果此 UUID 不是基於時間的 UUID，則此方法抛出 UnsupportedOperationException。
     *
     * @return 此 {@code UUID} 的節點值
     * @throws UnsupportedOperationException 如果此 UUID 的 version 不為 1
     */
    public long node() throws UnsupportedOperationException {
        checkTimeBase();
        return leastSigBits & 0x0000FFFFFFFFFFFFL;
    }

    /**
     * 返回此{@code UUID} 的字串表現形式。
     *
     * <p>
     * UUID 的字串表示形式由此 BNF 描述：
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @return 此{@code UUID} 的字串表現形式
     * @see #toString(boolean)
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * 返回此{@code UUID} 的字串表現形式。
     *
     * <p>
     * UUID 的字串表示形式由此 BNF 描述：
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param isSimple 是否簡單模式，簡單模式為不帶'-'的UUID字串
     * @return 此{@code UUID} 的字串表現形式
     */
    public String toString(boolean isSimple) {
        final StringBuilder builder = new StringBuilder(isSimple ? 32 : 36);
        // time_low
        builder.append(digits(mostSigBits >> 32, 8));
        if (!isSimple) {
            builder.append('-');
        }
        // time_mid
        builder.append(digits(mostSigBits >> 16, 4));
        if (!isSimple) {
            builder.append('-');
        }
        // time_high_and_version
        builder.append(digits(mostSigBits, 4));
        if (!isSimple) {
            builder.append('-');
        }
        // variant_and_sequence
        builder.append(digits(leastSigBits >> 48, 4));
        if (!isSimple) {
            builder.append('-');
        }
        // node
        builder.append(digits(leastSigBits, 12));

        return builder.toString();
    }

    // Comparison Operations

    /**
     * 返回此 UUID 的哈希碼。
     *
     * @return UUID 的哈希碼值。
     */
    @Override
    public int hashCode() {
        long hilo = mostSigBits ^ leastSigBits;
        return ((int) (hilo >> 32)) ^ (int) hilo;
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Private method start

    /**
     * 將此物件與指定物件比較。
     * <p>
     * 當且僅當參數不為 {@code null}、而是一個 UUID 物件、具有與此 UUID 相同的 varriant、包含相同的值（每一位均相同）時，結果才為 {@code true}。
     *
     * @param obj 要與之比較的物件
     * @return 如果物件相同，則返回 {@code true}；否則返回 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != UUID.class)) {
            return false;
        }
        UUID id = (UUID) obj;
        return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
    }

    /**
     * 將此 UUID 與指定的 UUID 比較。
     *
     * <p>
     * 如果兩個 UUID 不同，且第一個 UUID 的最高有效欄位大於第二個 UUID 的對應欄位，則第一個 UUID 大於第二個 UUID。
     *
     * @param val 與此 UUID 比較的 UUID
     * @return 在此 UUID 小於、等於或大於 val 時，分別返回 -1、0 或 1。
     *
     */
    @Override
    public int compareTo(UUID val) {
        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        return (this.mostSigBits < val.mostSigBits ? -1 : //
                (this.mostSigBits > val.mostSigBits ? 1 : //
                        (this.leastSigBits < val.leastSigBits ? -1 : //
                                (this.leastSigBits > val.leastSigBits ? 1 : //
                                        0))));
    }

    /**
     * 檢查是否為time-based版本UUID
     */
    private void checkTimeBase() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    /**
     * SecureRandom 的單例
     *
     */
    private static class Holder {
        static final SecureRandom numberGenerator = getSecureRandom();
    }
}

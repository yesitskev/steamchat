package com.kevelbreh.steamchat.steam.security;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by kevin on 2014/08/12.
 */
public class AsnKeyParser {

    final AsnParser _parser;

	/*public AsnKeyParser(String pathname) {
		using (var reader = new BinaryReader(
		  new FileStream(pathname, FileMode.Open, FileAccess.Read)))
		{
			var info = new FileInfo(pathname);

			_parser = new AsnParser(reader.ReadBytes((int)info.Length));
		}
	}*/

    public AsnKeyParser(List<Byte> contents) {
        _parser = new AsnParser(contents);
    }

    public static byte[] trimLeadingZero(byte[] values) {
        byte[] r;
        if (0x00 == values[0] && values.length > 1) {
            r = new byte[values.length - 1];
            System.arraycopy(values, 1, r, 0, values.length - 1);
        } else {
            r = new byte[values.length];
            System.arraycopy(values, 0, r, 0, values.length);
        }

        return r;
    }

    public static boolean equalOid(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }

        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public BigInteger[] parseRSAPublicKey() throws BerDecodeException {
        final BigInteger[] parameters = new BigInteger[2];

        // Current value

        // Sanity Check

        // Checkpoint
        int position = _parser.currentPosition();

        // Ignore Sequence - PublicKeyInfo
        int length = _parser.nextSequence();
        if (length != _parser.remainingBytes()) {
            final StringBuilder sb = new StringBuilder("Incorrect Sequence Size. ");
            sb.append(String.format("Specified: %d, Remaining: %d", length, _parser.remainingBytes()));
            throw new BerDecodeException(sb.toString(), position);
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore Sequence - AlgorithmIdentifier
        length = _parser.nextSequence();
        if (length > _parser.remainingBytes()) {
            final StringBuilder sb = new StringBuilder("Incorrect AlgorithmIdentifier Size. ");
            sb.append(String.format("Specified: %d, Remaining: %d", length, _parser.remainingBytes()));
            throw new BerDecodeException(sb.toString(), position);
        }

        // Checkpoint
        position = _parser.currentPosition();
        // Grab the OID
        final byte[] value = _parser.nextOID();
        final byte[] oid = { (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d, (byte) 0x01, (byte) 0x01, (byte) 0x01 };
        if (!AsnKeyParser.equalOid(value, oid)) {
            throw new BerDecodeException("Expected OID 1.2.840.113549.1.1.1", position);
        }

        // Optional Parameters
        if (_parser.IsNextNull()) {
            _parser.NextNull();
            // Also OK: value = _parser.Next();
        } else {
            // Gracefully skip the optional data
            _parser.Next();
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore BitString - PublicKey
        length = _parser.NextBitString();
        if (length > _parser.remainingBytes()) {
            final StringBuilder sb = new StringBuilder("Incorrect PublicKey Size. ");
            sb.append(String.format("Specified: %d, Remaining: %d", length, _parser.remainingBytes()));
            throw new BerDecodeException(sb.toString(), position);
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore Sequence - RSAPublicKey
        length = _parser.nextSequence();
        if (length < _parser.remainingBytes()) {
            final StringBuilder sb = new StringBuilder("Incorrect RSAPublicKey Size. ");
            sb.append(String.format("Specified: %d, Remaining: %d", length, _parser.remainingBytes()));
            throw new BerDecodeException(sb.toString(), position);
        }

        parameters[0] = new BigInteger(1, AsnKeyParser.trimLeadingZero(_parser.nextInteger()));
        parameters[1] = new BigInteger(1, AsnKeyParser.trimLeadingZero(_parser.nextInteger()));

        return parameters;
    }
}

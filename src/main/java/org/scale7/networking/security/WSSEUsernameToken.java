package org.scale7.networking.security;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Encoder;

public class WSSEUsernameToken {

    String _username;
    String _passwordDigest;
    String _nonce;
    Date _created;
    Boolean _isParsed;

    public WSSEUsernameToken()
    {
    }

    /// <summary>
    /// Parse the username token, and validate its format at the same time.
    /// </summary>
    /// <param name="usernameToken">The username token to validate.</param>
    /// <returns>Whether the username token has a valid format.</returns>
    public boolean parseFrom(String usernameToken)
    {
        String[] parts = usernameToken.split(",");
        if (parts.length != 4)
        {
            return false;
        }

        try
        {
            _username = parts[0].substring(24, 24 + parts[0].length() - 25);
            _passwordDigest = parts[1].substring(17, 17 + parts[1].length() - 18);
            _nonce = parts[2].substring(8, 8 + parts[2].length() - 9);
            String created = parts[3].substring(10, 10 + parts[3].length() - 11);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            _created = format.parse(created);
        }
        catch (Exception e)
        {
            return false;
        }

        _isParsed = true;

        return true;
    }

    /// <summary>
    /// Determine whether the timestamp used to create the username token is valid. The timestamp cannot represent
    /// a time in the future. The timestamp cannot be too old. A specified variance between the client computer's clock
    /// and the server computer's clock is allowed.
    /// </summary>
    /// <param name="maxTokenAge">The maximum allowed age of the token.</param>
    /// <param name="allowedClockVariance">The allowed variance between the client's computer clock and the server's computer clock.</param>
    /// <returns>Whether the username token has an allowed timestamp.</returns>
    public boolean isTimestampValid(int maxTokenAge, int allowedClockVariance) {

    	return isTimestampValid(_created, maxTokenAge, allowedClockVariance);
    }

    public static boolean isTimestampValid(Date created, int maxTokenAge, int allowedClockVariance) {
        Date now = new Date();

        // Get best version of token age based upon allowed variance between client and server
        // clocks.
        if (created.after(now))
        {
        	long adjustedCreated = created.getTime() - allowedClockVariance;
        	if (adjustedCreated > now.getTime())
                return false;

            return true;
        }

        long diff = now.getTime() - created.getTime();
        diff -= (diff > allowedClockVariance) ? allowedClockVariance : diff;

        if (diff < maxTokenAge)
        	return true;

        return false;
    }

    /// <summary>
    /// Does the username token have a valid signature in consideration of the correct password hash.
    /// </summary>
    /// <param name="passwordHash">The password hash corresponding to the user.</param>
    /// <returns>Whether the signature is valid.</returns>
    public boolean isSignatureValid(String passwordHash)
    {
    	String usernameTokenHash;
    	try {
    		usernameTokenHash = generateUsernameTokenHash(_nonce, _created, passwordHash);
    	}
    	catch (Exception e)
    	{
    		return false;
    	}

        return usernameTokenHash.equals(_passwordDigest);
    }

    /// <summary>
    /// The user identity that is conveyed by the token.
    /// </summary>
    public String getUsername()
    {
        return _username;
    }
    public void setUsername(String username)
    {
        _username = username;
    }

    /// <summary>
    /// The time that this username token was created by the client.
    /// </summary>
    public Date getCreated()
    {
    	return _created;
    }

    public void setCreated(Date created)
    {
    	_created = created;
    }


    /// <summary>
    /// Extract the user id encoded into a username token.
    /// </summary>
    /// <param name="usernameToken">The username token containing the user id.</param>
    /// <returns>The encoded user id.</returns>
    public static String getUsernameFromToken(String usernameToken)
    {
        String[] parts = usernameToken.split(",");
        if (parts.length != 4)
            return null;

        String username = parts[0].substring(24, 24 + parts[0].length() - 25);

        assert isValidUsername(username);

        return username;
    }

    /// <summary>
    /// Returns whether a username token is valid. A username token is supplied in place of a normal password during secure
    /// communications.
    /// </summary>
    /// <param name="usernameToken">The username token to be validated.</param>
    /// <param name="passwordHash">A SHA1 hash of the password.</param>
    /// <param name="maxTimeToCreated">The max allowed age of the username token supplied.</param>
    /// <returns>Whether the token is valid.</returns>
    public static boolean authenticateUsernameToken(String usernameToken, String passwordHash, int maxTokenAge, int allowedClockVariance) throws Exception
    {
        String[] parts = usernameToken.split(",");
        if (parts.length != 4)
            return false;

        String passwordDigest = parts[1].substring(17, 17 + parts[1].length() - 18);
        String nonce = parts[2].substring(8, 8 + parts[2].length() - 9);
        String created = parts[3].substring(10, 10 + parts[3].length() - 11);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date tokenCreated = format.parse(created);

        if (!isTimestampValid(tokenCreated, maxTokenAge, allowedClockVariance))
        	return false;

        String usernameTokenHash = generateUsernameTokenHash(nonce, tokenCreated, passwordHash);

        return usernameTokenHash.equals(passwordDigest);
    }

    /// <summary>
    /// Generates a Web Services Security username token. Implementation based on algorithm description at
    /// http://www.oasis-open.org/committees/wss/documents/WSS-Username-02-0223-merged.pdf
    /// </summary>
    /// <param name="username">The username.</param>
    /// <param name="nonce">A cryptographically random nonce.</param>
    /// <param name="created">The time at which the token is generated.</param>
    /// <param name="passwordHash">A SHA1 hash of the password.</param>
    /// <returns>The username token.</returns>
    public static String generateUsernameToken(String username, String nonce, Date created, String passwordHash) throws Exception
    {
        assert isValidUsername(username);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return "UsernameToken Username=\"" + username + "\", " +
               "PasswordDigest=\"" + generateUsernameTokenHash(nonce, created, passwordHash) + "\", " +
               "Nonce=\"" + nonce + "\", " +
               "Created=\"" + format.format(created) + "\"";
    }

    /// <summary>
    /// Generates a Web Services Security username token hash. Implementation based on algorithm description at
    /// http://www.oasis-open.org/committees/wss/documents/WSS-Username-02-0223-merged.pdf
    /// </summary>
    /// <param name="nonce">A cryptographically random nonce.</param>
    /// <param name="created">The time at which the token is generated.</param>
    /// <param name="passwordHash">A SHA1 hash of the password</param>
    /// <returns>The hash of the token.</returns>
    public static String generateUsernameTokenHash(String nonce, Date created, String passwordHash) throws Exception
    {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	format.setTimeZone(TimeZone.getTimeZone("UTC"));

        byte[] n = nonce.getBytes("UTF8");
        byte[] c = format.format(created).getBytes("UTF8");
        byte[] p = passwordHash.getBytes("UTF8");
        byte[] toBeDigested = new byte[n.length + c.length + p.length];

        System.arraycopy(n, 0, toBeDigested, 0, n.length);
        System.arraycopy(c, 0, toBeDigested, n.length, c.length);
        System.arraycopy(p, 0, toBeDigested, (n.length + c.length), p.length);

        // protect password hash
        ByteBuffer.wrap(p).clear();

        // calculate SHA1 digest
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        md.update(toBeDigested);
        byte[] digest = md.digest();

        // protected password hash again
        ByteBuffer.wrap(toBeDigested).clear();

        //return new Base64().encodeToString(digest);
        return new sun.misc.BASE64Encoder().encode(digest);
    }

    /// <summary>
    /// Generate a cryptographically random nonce.
    /// </summary>
    /// <returns>The cryptographically random nonce.</returns>
    public static String generateNonce()
    {
        byte[] nonce = new byte[16];
        (new Random()).nextBytes(nonce);

        //return new Base64().encodeToString(nonce);
        return new sun.misc.BASE64Encoder().encode(nonce);
    }

    /// <summary>
    /// Generates a secure hash of a password using SHA1
    /// </summary>
    /// <param name="password">The password to be hashed.</param>
    /// <returns>The base 64 encoded hash</returns>
    public static String generatePasswordHash(String password) throws Exception
    {
        byte[] ph = password.getBytes("UTF8");
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        md.update(ph);
        byte[] digest = md.digest();

        // protected password hash again
        ByteBuffer.wrap(ph).clear();

        //return new Base64().encodeToString(digest);
        return new sun.misc.BASE64Encoder().encode(digest);
    }

    /// <summary>
    /// Determines whether a username format is valid for usage with this scheme.
    /// </summary>
    /// <param name="username">The username to test.</param>
    /// <returns>Whether the username has a valid format for usage with username tokens.</returns>
    public static Boolean isValidUsername(String username)
    {
    	Pattern pattern = Pattern.compile("[^a-zA-Z0-9-]");
    	Matcher matcher = pattern.matcher(username);
    	return matcher.matches() == false;
    }
}

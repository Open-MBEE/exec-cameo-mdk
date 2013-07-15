package gov.nasa.jpl.mbee.docweb;


import java.security.Security;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class HttpsUtils {

	public static void allowSelfSignedCertificates() {
		// Remove a security provider installed by MagicDraw
		// that throws weird exceptions when using SSL.
		Security.removeProvider("Certicom");

		// Register an HTTPS factory that allows self-signed SSL certificates.
		Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);
	}


}

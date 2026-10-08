package com.amit.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@Service
public class CountryRestrictionService {

    private static final Set<String> RESTRICTED_COUNTRIES = Set.of(
            "SY", // Syria
            "AF", // Afghanistan
            "IR"  // Iran
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${country.restriction.test-mode:false}")
    private boolean testMode;

    public boolean isSignupAllowed(HttpServletRequest request) {

        /*
         * Test mechanism.
         *
         * Only works when:
         * country.restriction.test-mode=true
         *
         * Example:
         * X-Test-Country: SY
         */
        if (testMode) {

            String testCountry = request.getHeader("X-Test-Country");

            if (testCountry != null && !testCountry.isBlank()) {

                String countryCode =
                        testCountry.trim().toUpperCase();

                return !RESTRICTED_COUNTRIES.contains(countryCode);
            }
        }

        String clientIp = getClientIp(request);

        // Allow local development requests.
        if (isLocalIp(clientIp)) {
            return true;
        }

        String countryCode = getCountryCode(clientIp);

        /*
         * If country lookup fails, allow signup.
         * This prevents an external country API failure
         * from blocking legitimate users.
         */
        return countryCode == null
                || !RESTRICTED_COUNTRIES.contains(countryCode);
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp =
                request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isLocalIp(String ip) {

        if (ip == null || ip.isBlank()) {
            return true;
        }

        try {

            InetAddress address =
                    InetAddress.getByName(ip);

            return address.isLoopbackAddress()
                    || address.isSiteLocalAddress();

        } catch (Exception e) {
            return true;
        }
    }

    private String getCountryCode(String ip) {

        try {

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://ipapi.co/"
                                    + ip
                                    + "/country/"
                            ))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 200) {

                return response.body()
                        .trim()
                        .toUpperCase();
            }

        } catch (Exception e) {

            // Do not block signup if lookup service fails.
        }

        return null;
    }
}


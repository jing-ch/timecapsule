package edu.northeastern.timecapsule.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Wrapper for runtime permission checks, fused location lookup, and reverse geocoding.
 */
public class LocationHelper {

    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private final FragmentActivity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Geocoder geocoder;
    private final ActivityResultLauncher<String[]> permissionLauncher;

    private LocationResultCallback pendingCallback;

    public LocationHelper(@NonNull FragmentActivity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.geocoder = new Geocoder(activity, Locale.getDefault());
        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                            || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                    if (!granted) {
                        notifyError("Location permission denied");
                        return;
                    }

                    requestLocationInternal();
                }
        );
    }

    public void fetchCurrentLocation(@NonNull LocationResultCallback callback) {
        pendingCallback = callback;

        if (hasLocationPermission()) {
            requestLocationInternal();
            return;
        }

        permissionLauncher.launch(LOCATION_PERMISSIONS);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationInternal() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        reverseGeocode(location);
                        return;
                    }

                    CancellationTokenSource tokenSource = new CancellationTokenSource();
                    fusedLocationClient.getCurrentLocation(
                                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                                    tokenSource.getToken()
                            )
                            .addOnSuccessListener(currentLocation -> {
                                if (currentLocation == null) {
                                    notifyError("Unable to get current location");
                                    return;
                                }
                                reverseGeocode(currentLocation);
                            })
                            .addOnFailureListener(e -> notifyError("Failed to get current location"));
                })
                .addOnFailureListener(e -> notifyError("Failed to access location"));
    }

    private void reverseGeocode(@NonNull Location location) {
        if (!Geocoder.isPresent()) {
            notifySuccess(formatCoordinates(location));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1,
                    new Geocoder.GeocodeListener() {
                        @Override
                        public void onGeocode(@NonNull List<Address> addresses) {
                            notifySuccess(formatAddress(addresses, location));
                        }

                        @Override
                        public void onError(String errorMessage) {
                            notifySuccess(formatCoordinates(location));
                        }
                    }
            );
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );
            notifySuccess(formatAddress(addresses, location));
        } catch (IOException e) {
            notifySuccess(formatCoordinates(location));
        }
    }

    private String formatAddress(List<Address> addresses, Location location) {
        if (addresses == null || addresses.isEmpty()) {
            return formatCoordinates(location);
        }

        Address address = addresses.get(0);
        String locality = firstNonEmpty(address.getLocality(), address.getSubAdminArea(), address.getAdminArea());
        String country = address.getCountryName();

        if (locality != null && country != null && !country.isEmpty()) {
            return locality + ", " + country;
        }
        if (locality != null) {
            return locality;
        }
        if (country != null && !country.isEmpty()) {
            return country;
        }
        return formatCoordinates(location);
    }

    private String formatCoordinates(Location location) {
        return String.format(
                Locale.US,
                "%.4f, %.4f",
                location.getLatitude(),
                location.getLongitude()
        );
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private void notifySuccess(String locationText) {
        if (pendingCallback == null) {
            return;
        }

        pendingCallback.onLocationResolved(locationText);
        pendingCallback = null;
    }

    private void notifyError(String message) {
        if (pendingCallback == null) {
            return;
        }

        pendingCallback.onLocationError(message);
        pendingCallback = null;
    }

    public interface LocationResultCallback {
        void onLocationResolved(@NonNull String locationText);

        void onLocationError(@NonNull String message);
    }
}

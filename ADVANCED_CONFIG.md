# ⚙️ Advanced Configurations

## 🎯 Tuning Parameters

### HandDetector Configuration
```kotlin
// In HandDetector.kt

// Should be set to 1 (detect only one hand at a time)
const val MAX_HANDS = 1

// false: Optimized for video (faster tracking)
// true: Optimized for individual images (slower but more precise)
STATIC_IMAGE_MODE = false

// Utilize GPU for high-performance processing
RUN_ON_GPU = true
```

### RotationDetector Tuning
```kotlin
// Window size for moving average calculation
val bufferSize = 10  // Recommended range: 5-20

// Minimum confidence threshold for valid detection
const val CONFIDENCE_THRESHOLD = 0.6f  // Range: 0.5-0.8

// Minimum rotation angle to trigger an event (degrees)
const val ANGLE_THRESHOLD = 2f  // Range: 1-5 degrees

// Minimum confidence required for the averaged rotation result
const val AVERAGE_ROTATION_CONFIDENCE = 0.6f
```

### Volume Control Timing
```kotlin
// Cooldown period between rotation-triggered actions
const val ROTATION_COOLDOWN = 500L  // Range: 300-1000ms

// Prevents volume "flickering" 
// Higher values = smoother but less responsive control
```

### Driving Mode Detection
```kotlin
// Minimum speed to be classified as "Driving"
const val DRIVING_SPEED_THRESHOLD = 5f  // km/h

// Frequency of GPS updates
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    1000,      // 1000ms interval between updates
    10f,       // 10 meters minimum displacement
    listener
)

// Faster updates lead to higher battery consumption
```

## 📊 Performance Metrics

### Expected Performance

```
Device: Pixel 5 (Snapdragon 765)
Display: 6" FHD+

Hand Detection:    ~30 FPS (33ms per frame)
Rotation Detection: ~60 FPS (16ms per frame)
Total Latency:      50-100ms
CPU Usage:          15-25%
Memory Footprint:   80-120 MB
Battery Drain:      8-10% per hour (with active GPS)
```

### Optimization Checklist

- ✅ Enable **GPU rendering** in MediaPipe.
- ✅ Limit **buffer sizes** (max 10 frames).
- ✅ Ensure all unused resources are **closed/released**.
- ✅ Implement **back-pressure strategy** in CameraX.
- ✅ Disable **GPS tracking** when not in driving mode.
- ✅ Optimize **update frequency** based on state.

## 🔐 Security Best Practices

### Camera Usage
- **Do not save** raw camera frames to local storage.
- **Do not transmit** video data to servers without end-to-end encryption.
- **Immediately discard** processed frames after landmark extraction.

### Location Privacy
- **Avoid linking** location metadata directly with camera frames.
- **Do not share** location data with 3rd party providers.
- **Clear history** periodically (e.g., every 1 hour).

### Permissions
- **Always verify** permissions before starting any core service.
```kotlin
if (!PermissionManager.hasPermission(context, CAMERA)) {
    // Gracefully stop the service
}
```

## 🚀 Custom Configuration Profiles

### Light Driving Mode (Low Power)
- Polls GPS only.
- Does not initiate hand detection until movement is confirmed.
- Prioritizes battery longevity.

### Aggressive Mode (High Sensitivity)
- `bufferSize = 5` (Faster response).
- `confidence = 0.4` (More sensitive).
- `cooldown = 300ms` (Rapid triggers).
- ⚠️ *Note: Higher risk of false positives.*

### Conservative Mode (High Stability)
- `bufferSize = 20` (Smoother average).
- `confidence = 0.8` (Highly reliable).
- `cooldown = 1000ms` (Measured response).
- ✅ *Note: Minimal false positives.*

## 📈 Monitoring & Logging

### Log Levels
- `Log.v()`: Verbose - Detailed debugging information.
- `Log.d()`: Debug - General operational logs.
- `Log.i()`: Info - Major state changes.
- `Log.w()`: Warning - Non-critical issues.
- `Log.e()`: Error - Critical failures.

### Metrics Collection
```kotlin
data class PerformanceMetrics(
    val fps: Float,
    val latency: Long,
    val cpuUsage: Float,
    val memoryUsage: Long,
    val batteryDrain: Float
)
```

## 🔄 Continuous Integration

### Suggested Testing Frameworks
```gradle
testImplementation 'junit:junit:4.13.2'
testImplementation 'io.mockk:mockk:1.13.5'
androidTestImplementation 'androidx.test:runner:1.5.2'
```

### Key Test Cases
- [ ] Hand detection under varying lighting conditions.
- [ ] Rotation detection precision and direction accuracy.
- [ ] Volume control system integration.
- [ ] GPS-based driving mode entry/exit triggers.
- [ ] Foreground service lifecycle stability.
- [ ] Permission denial/revocation handling.
- [ ] Memory leak detection under long-duration usage.

---

**Last Updated: May 2026**
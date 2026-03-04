# Google Maps API Setup Guide

This guide will help you set up Google Maps API for the Re:Buy app's recycling center map feature.

## Prerequisites
- Google Cloud Platform account
- Android Studio installed
- Re:Buy app source code

## Step-by-Step Setup

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name (e.g., "Re-Buy-Map")
4. Click "Create"

### 2. Enable Maps SDK for Android

1. In the Cloud Console, go to "APIs & Services" → "Library"
2. Search for "Maps SDK for Android"
3. Click on it and press "ENABLE"

### 3. Create API Credentials

1. Go to "APIs & Services" → "Credentials"
2. Click "CREATE CREDENTIALS" → "API key"
3. Copy the API key that appears

### 4. Restrict Your API Key (Recommended)

For security, restrict your API key:

1. Click on your newly created API key
2. Under "Application restrictions":
   - Select "Android apps"
   - Click "ADD AN ITEM"
   - Package name: `com.yourcompany.re_buy`
   - Get your SHA-1 fingerprint by running:
     ```bash
     cd C:\Android\Re_Buy
     gradlew.bat signingReport
     ```
   - Copy the SHA-1 from the debug keystore
   - Paste it in the "SHA-1 certificate fingerprint" field

3. Under "API restrictions":
   - Select "Restrict key"
   - Check "Maps SDK for Android"
   - Click "SAVE"

### 5. Add API Key to Your App

Edit `app/src/main/AndroidManifest.xml` and replace the placeholder:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_ACTUAL_API_KEY_HERE" />
```

Replace `YOUR_ACTUAL_API_KEY_HERE` with your actual API key from step 3.

### 6. Build and Run

1. Clean and rebuild the project:
   ```bash
   cd C:\Android\Re_Buy
   gradlew.bat clean
   gradlew.bat assembleDebug
   ```

2. Install on your device or emulator
3. Navigate to the "지도" (Map) tab
4. You should see the map with 13 recycling center markers

## Map Features

### Search Functionality
- Search bar at the top allows you to search by:
  - Center name (e.g., "종로구")
  - District name (e.g., "강남구")
  - Address
- Real-time filtering as you type
- Clear button (X) appears when text is entered

### Filter Chips
- Horizontal scrollable chips below the search bar
- "전체" (All) chip shows all centers
- Individual district chips filter centers by district
- Multiple districts can be selected at once
- Green highlight indicates selected filters

### Map Markers
- Green markers indicate recycling center locations
- Click any marker to see details
- Map automatically zooms to show filtered results

### Info Card
- Appears at the bottom when a marker is clicked
- Shows:
  - Center name with green recycling icon
  - Full address with location icon
  - Phone number with call icon
- Action buttons:
  - **전화하기** (Call): Opens dialer with the number
  - **길찾기** (Directions): Opens Google Maps for navigation
  - **웹사이트** (Website): Opens the center's website
  - **X** (Close): Closes the info card

### Map Controls
- Zoom controls (+/-)
- Compass for orientation
- Tap anywhere on the map to close the info card
- Smooth animations for camera movements

## Troubleshooting

### Map is blank or shows "This page can't load Google Maps correctly"
- **Cause**: Invalid or missing API key
- **Solution**: Check that you've added a valid API key in AndroidManifest.xml

### Map is gray with no markers
- **Cause**: API key restrictions may be blocking the app
- **Solution**:
  - Check that your package name matches: `com.yourcompany.re_buy`
  - Verify your SHA-1 fingerprint is correct
  - Try using an unrestricted API key for testing (not recommended for production)

### Markers don't appear
- **Cause**: Data or coordinate issues
- **Solution**:
  - Check that RecyclingCenter.kt has valid coordinates
  - Ensure the map is zoomed to Seoul area

### App crashes on map tab
- **Cause**: Missing Google Play Services
- **Solution**:
  - Ensure your device has Google Play Services installed
  - Update Google Play Services if needed

## API Usage and Billing

- Google provides $200 free credit per month
- Map loads and interactions are metered
- Monitor usage in [Google Cloud Console](https://console.cloud.google.com/billing)
- Set up billing alerts to avoid unexpected charges

## Current Data

The map currently displays **13 recycling centers in Seoul**:

1. 종로구 재활용센터
2. 중구 재활용센터
3. 용산구 재활용센터
4. 도봉구 재활용센터
5. 서대문구 재활용센터
6. 성북구 재활용센터
7. 강동구 재활용센터
8. 마포구 재활용센터
9. 중랑구 재활용센터
10. 영등포구 재활용센터
11. 은평구 재활용센터
12. 강서구 재활용센터
13. 동대문구 재활용센터

Each center includes:
- Accurate GPS coordinates
- Complete address
- Phone number
- Website URL

## Future Enhancements

To expand the map to nationwide coverage:

1. **Add More Data**: Update `RecyclingCenter.kt` with centers from other cities
2. **Database Integration**: Consider moving data to Firebase Firestore for easier updates
3. **User Contributions**: Allow users to suggest new recycling centers
4. **Clustering**: For large numbers of markers, implement marker clustering
5. **Location Services**: Add "Near Me" feature to show closest centers
6. **Navigation**: Direct integration with Google Maps directions

## Support

For issues or questions:
- Check [Google Maps Platform Documentation](https://developers.google.com/maps/documentation/android-sdk)
- Review [Stack Overflow](https://stackoverflow.com/questions/tagged/google-maps-android-api-2)
- Contact the development team

---

**Last Updated**: October 2025
**App Version**: 1.0
**Maps SDK Version**: 18.2.0

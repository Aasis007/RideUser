package Common;

import com.example.laptop.rideuser.Retrofit.FCMClient;
import com.example.laptop.rideuser.Retrofit.IFCMService;

/**
 * Created by Laptop on 2/11/2018.
 */

public class Common {
    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "Driversinformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "Pickuprequest";
    public static final String token_tbl = "Tokens";


    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethod;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.MerchantPayment;
import com.mercadopago.util.LayoutUtil;
import com.squareup.okhttp.Response;

import java.math.BigDecimal;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;

public class Utils {

    public static final int CARD_REQUEST_CODE = 13;

    //Custom your MercadoPago PUBLIC_KEY and ACCESS_TOKEN
    public static String PUBLIC_KEY = "TEST-d840bf4c-ee1e-4563-8813-7707e73e1c05";
    public static String ACCESS_TOKEN = "TEST-2919128858572145-060511-c777c7825bfb2a8c43627d88f4bbcff1__LC_LD__-184272638";

    //Custom your BASE_URL and CREATE_PAYMENT_URI if you dont have an api
    public static final String BASE_URL = "https://www.mercadopago.com";
    public static final String CREATE_PAYMENT_URI = "/checkout/examples/doPayment";

    //Only for test
    public static final String DUMMY_ITEM_ID = "id1";
    public static final Integer DUMMY_ITEM_QUANTITY = 1;
    public static final BigDecimal DUMMY_ITEM_UNIT_PRICE = new BigDecimal("100");

    public static void startCardActivity(Activity activity, String publicKey, PaymentMethod paymentMethod) {
        Intent cardIntent = new Intent(activity, CardActivity.class);
        cardIntent.putExtra("merchantPublicKey", publicKey);
        cardIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
        activity.startActivityForResult(cardIntent, CARD_REQUEST_CODE);
    }

    public static void createPayment(final Activity activity, String token, final Integer installments, Long cardIssuerId, BigDecimal price,
                                     final PaymentMethod paymentMethod, Discount discount) {

        if (paymentMethod != null) {
            Item item = new Item(DUMMY_ITEM_ID, DUMMY_ITEM_QUANTITY, price);

            final String paymentMethodId = paymentMethod.getId();
            Long campaignId = (discount != null) ? discount.getId() : null;
            MerchantPayment merchantPayment = new MerchantPayment(item, installments, cardIssuerId, token, paymentMethodId, campaignId, ACCESS_TOKEN);

            //Custom your payment method api call
            //In this case:
            App.service.payOrder(order.id,
                    token,
                    installments,
                    paymentMethodId,
                    email,
                    new Callback<HashMap<String, Object>>() {
                        @Override
                        public void success(HashMap<String, Object> hashMap, Response response) {

                            Integer status = Integer.parseInt((String) hashMap.get("status"));
                            if (status == 200 || status == 201){
                                Gson gson = new GsonBuilder()
                                        .serializeNulls()
                                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                        .create();
                                String serializedPayment = gson.toJson(hashMap.get("response"));
                                Payment payment = gson.fromJson(serializedPayment, Payment.class);
                                new MercadoPago.StartActivityBuilder()
                                        .setActivity(activity)
                                        .setPayment(payment)
                                        .setPaymentMethod(paymentMethod)
                                        .startCongratsActivity();

                            } else {
                                Toast.makeText(activity, "Ocurrió un error al procesar el pago", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            LayoutUtil.showRegularLayout(activity);
                            Toast.makeText(activity, "Ocurrió un error al procesar el pago", Toast.LENGTH_LONG).show();
                        }

                    });
        } else {
            Toast.makeText(activity, "Invalid payment method", Toast.LENGTH_LONG).show();
        }
    }

}
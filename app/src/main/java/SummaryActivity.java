import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.mercadopago.MainActivity;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethod;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.util.LayoutUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends Activity {

    //Son los metodos de pago que queremos que admita MercadoPago a la hora de pagar
    private List<String> supportedPaymentTypes = new ArrayList<String>() {
        add("credit_card");
    };

    //Este método debe ser llamado en el onClick de algun boton que dé inicio al proceso de pago
    public void send(View view) {
        new MercadoPago.StartActivityBuilder()
                .setActivity(SummaryActivity.this)
                .setPublicKey(Utils.PUBLIC_KEY)
                .setSupportedPaymentTypes(supportedPaymentTypes)
                .startPaymentMethodsActivity();
    }

    //Este método es el encargado de manejar las respuestas de las activities que usa MercadoPago en el proceso
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MercadoPago.PAYMENT_METHODS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);
                Utils.startCardActivity(this, Utils.PUBLIC_KEY, paymentMethod);
            } else {
                if ((data != null) && (data.getStringExtra("apiException") != null)) {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == Utils.CARD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    cardToken = data.getStringExtra("token");
                    Utils.createPayment(this,
                            data.getStringExtra("token"),
                            1, //Custom installments
                            null,
                            BigDecimal.valueOf(100), //Custom price
                            JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class), null);
                } catch (Exception exc) {
                    exc.printStackTrace();
                    Toast.makeText(this, "Ocurrio un error al procesar al pago", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (data != null) {
                    if (data.getStringExtra("apiException") != null) {
                        Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                    } else if (data.getBooleanExtra("backButtonPressed", false)) {
                        new MercadoPago.StartActivityBuilder()
                                .setActivity(this)
                                .setPublicKey(Utils.PUBLIC_KEY)
                                .setSupportedPaymentTypes(supportedPaymentTypes)
                                .startPaymentMethodsActivity();
                    }
                }
            }
        } else if (requestCode == MercadoPago.CONGRATS_REQUEST_CODE) {
            LayoutUtil.showRegularLayout(this);

            Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }
    }

}
package com.astinil.AndroidTimesheet.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.astinil.AndroidTimesheet.R;
import com.astinil.AndroidTimesheet.api.ApiClient;
import com.astinil.AndroidTimesheet.api.ApiService;
import com.astinil.AndroidTimesheet.api.model.CheckOutDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckinDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_checkin, null);
        EditText etNote = v.findViewById(R.id.etNote);
        Button btnSubmit = v.findViewById(R.id.btnSubmit);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(v);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(view -> {
            String note = etNote.getText().toString();

            if (TextUtils.isEmpty(note)) {
                Toast.makeText(getContext(), "Enter a note", Toast.LENGTH_SHORT).show();
            } else {
                sendCheckin(dialog);
            }
        });

        return dialog;
    }

    private void sendCheckin(Dialog dialog) {

        ApiService api = ApiClient.getSecuredApi(requireContext()); // TOKEN ENABLED

        api.checkInCheckout().enqueue(new Callback<CheckOutDto>() {

            @Override
            public void onResponse(Call<CheckOutDto> call, Response<CheckOutDto> response) {

                dialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {

                    CheckOutDto data = response.body();

                    Toast.makeText(getContext(),
                            "Checked in at: " + data.checkInTime,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Check-in failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckOutDto> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

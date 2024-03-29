package com.example.tcc_sgd;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    private BancoFirestore metodoBanco = new BancoFirestore(); //Objeto para os metodos do banco
    private TextView email, nome, telefone, data_nasc, alterarFoto, tirarFoto, galeriaFoto, cancelarFoto, removerFoto;
    private Button deslogar;
    private ImageView imageViewNome, imageViewTelefone, imageViewDataNasc, imageViewEmail, imageFoto;
    private String[] nomeFinal = new String[2];
    private CircleImageView imagemPerfilMenu;
    private CircleImageView imagemPerfil;
    private View view;
    private FirebaseFirestore feed = FirebaseFirestore.getInstance();
    private String usuarioID;
    private AlertDialog.Builder builderDialog;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private Uri fotoDeletar;
    private Object Uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //PEGANDO IDS
        email = view.findViewById(R.id.textViewEmail);
        nome = view.findViewById(R.id.textViewNome);
        telefone = view.findViewById(R.id.textViewtelefone);
        data_nasc = view.findViewById(R.id.textViewDataNascimento);
        deslogar = view.findViewById(R.id.buttonDeslogar);
        imageViewNome = view.findViewById(R.id.imageViewNome);
        imageViewDataNasc = view.findViewById(R.id.imageViewDataNasc);
        imageViewTelefone = view.findViewById(R.id.imageViewTelefone);
        imagemPerfil = view.findViewById(R.id.imageViewPerfil);
        imagemPerfilMenu = getActivity().findViewById(R.id.imageViewMenu);
        imageViewEmail = view.findViewById(R.id.imageViewEmail1);
        imageFoto = view.findViewById(R.id.imageViewAlterarPerfil);

        //ATUALIZANDO FOTO DE PERFIL
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            if(user.getPhotoUrl() != null){
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(imagemPerfil);
            }else   imagemPerfil.setImageResource(R.drawable.sgdbottomsheet);

        }
        //CRIANDO CAMPO DE LOADING
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Estamos upando sua foto, por favor aguerde...");

        imageFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //criando a tela de opções
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        view.getContext(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(view.getContext().getApplicationContext())
                        .inflate(
                                R.layout.alterarfoto_layout,
                                (LinearLayout) view.findViewById(R.id.bottomSheetContainer)
                        );

                tirarFoto = bottomSheetView.findViewById(R.id.textViewTirarFoto);
                galeriaFoto = bottomSheetView.findViewById(R.id.textViewGaleria);
                cancelarFoto = bottomSheetView.findViewById(R.id.textViewCancelar);
                removerFoto = bottomSheetView.findViewById(R.id.textViewRemover);

                galeriaFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, 33);
                        bottomSheetDialog.dismiss();
                    }
                });
                tirarFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null){
                            startActivityForResult(intent, 10001);
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
                cancelarFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });
                removerFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                        if (String.valueOf(user.getPhotoUrl()) != "null"){
                      StorageReference storageReference = firebaseStorage.getReferenceFromUrl(String.valueOf(user.getPhotoUrl()));
                      storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void unused) {
                              imagemPerfil.setImageResource(R.drawable.sgdbottomsheet);
                              imagemPerfilMenu.setImageResource(R.drawable.sgdbottomsheet);
                              bottomSheetDialog.dismiss();
                              deleteFoto();
                          }
                      }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              Toast.makeText(view.getContext(), "Falha ao remover foto", Toast.LENGTH_SHORT).show();
                          }
                      });
                  } else Toast.makeText(view.getContext(), "Você não possui nenhuma foto para ser removida", Toast.LENGTH_SHORT).show();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
                }
        });

        //METEDO BOTÃO DESLOGAR
        deslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogSair(R.layout.dialog_confirmacao_sair);
            }
        });
        
        imageViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Não é possivel alterar o Email Cadastrado!", Toast.LENGTH_SHORT).show();
            }
        });        

        //METODO PARA ATUALIZAR AS INFORMAÇÕES DO USUARIO
        imageViewNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogAtualizarInfo(R.layout.dialog_atualizar_nome, 1);
            }
        });

        imageViewDataNasc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogAtualizarInfo(R.layout.dialog_atualizar_data, 2);
            }
        });

        imageViewTelefone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogAtualizarInfo(R.layout.dialog_atualizar_telefone,3);
            }
        });

        return view;

    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 33){
            switch (resultCode){
                case -1:
                    Uri imagem = (Uri) data.getData();
                    imagemPerfil.setImageURI(imagem);
                    imagemPerfilMenu.setImageURI(imagem);
                    handleUpload(imagem);
                    progressDialog.show();
            }
        }
        if (requestCode == 10001){
            switch (resultCode){
                case -1:
                    Bitmap imagem = (Bitmap) data.getExtras().get("data");
                    imagemPerfil.setImageBitmap(imagem);
                    imagemPerfilMenu.setImageBitmap(imagem);
                    handleUpload1(imagem);
                    progressDialog.show();
            }
        }
    }

    private void handleUpload1(Bitmap imagem){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(email.getText().toString() + ".jpeg");
        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Falha ao salvar a foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleUpload(Uri imagem){
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(email.getText().toString() + ".jpeg");
        reference.putFile(imagem)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Falha ao salvar a foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setUserProfileUrl(uri);

                    }
                });
    }

    private void deleteFoto() {
        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(null)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Foto deletada com sucesso", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Falha ao deletadar a foto de perfil", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void setUserProfileUrl(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Foto de perfil atualizada com sucesso", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Falha ao salvar a foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        metodoBanco.verificaLogin(getActivity());

        try {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            DocumentReference documentReference = feed.collection("Usuarios").document(usuarioID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if(documentSnapshot != null){
                        try {
                            nome = view.findViewById(R.id.textViewNome);
                            email = view.findViewById(R.id.textViewEmail);
                            telefone = view.findViewById(R.id.textViewtelefone);
                            data_nasc = view.findViewById(R.id.textViewDataNascimento);
                            nome.setText(documentSnapshot.getString("nome"));
                            email.setText(documentSnapshot.getString("email"));
                            data_nasc.setText(documentSnapshot.getString("data_nasc"));
                            telefone.setText(documentSnapshot.getString("telefone"));
                        } catch (Exception e){
                            Toast.makeText(view.getContext(), "ERRO", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        } catch (Exception e){
            Toast.makeText(view.getContext(), "", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAlertDialogAtualizarInfo(int layoutDialog, int requestCode) {
        builderDialog = new AlertDialog.Builder(view.getContext());
        View LayoutView = getLayoutInflater().inflate(layoutDialog, null);
        //BOTÕES DOS DIALOGS
        AppCompatButton dialogButtomAtualizar = LayoutView.findViewById(R.id.botao_atualizar);
        AppCompatButton dialogButtomAtualizar_data = LayoutView.findViewById(R.id.botao_atualizar_data);
        AppCompatButton dialogButtomAtualizar_telefone = LayoutView.findViewById(R.id.botao_atualizar_tel);
        AppCompatButton dialogButtonCancelar = LayoutView.findViewById(R.id.botao_cancel);
        //EDIT TEXT DOS DIALOGS
        EditText nomeNovo = LayoutView.findViewById(R.id.atualizarNome);
        EditText nomeNovo2 = LayoutView.findViewById(R.id.atualizarNome2);
        EditText dataNasc = LayoutView.findViewById(R.id.atualizarDataNasc);
        EditText telefoneNovo = LayoutView.findViewById(R.id.atualizarTel);
        //MONTANDO O DIALOG
        builderDialog.setView(LayoutView);
        alertDialog = builderDialog.create();
        alertDialog.show();

        if (requestCode == 1){
            //Criando a maskara do nome
            SimpleMaskFormatter nomeP = new SimpleMaskFormatter("LLLLLLLLLLLL");
            MaskTextWatcher ttw = new MaskTextWatcher(nomeNovo, nomeP);
            nomeNovo.addTextChangedListener(ttw);

            //Criando a maskara do sobrenome
            SimpleMaskFormatter nomeS = new SimpleMaskFormatter("LLLLLLLLLLLL");
            MaskTextWatcher rtw = new MaskTextWatcher(nomeNovo2, nomeS);
            nomeNovo2.addTextChangedListener(rtw);

            //SEPARANDO O NOME EM DUAS STRINGS
            String[] newStr = nome.getText().toString().split("\\s+");
            for (int i = 0; i < newStr.length; i++) {
                nomeFinal[i] = newStr[i];
            }
            //Atualizar Nome
            nomeNovo.setText(nomeFinal[0]);
            nomeNovo2.setText(nomeFinal[1]);
            dialogButtomAtualizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nomeCompleto = nomeNovo.getText().toString() + " " + nomeNovo2.getText().toString();
                    metodoBanco.atualizarInformacoes(email.getText().toString(), nomeCompleto, data_nasc.getText().toString()
                            , telefone.getText().toString(), view.getContext());
                    alertDialog.dismiss();
                }
            });
        }

        if (requestCode == 2){
            //Criando a maskara da data
            SimpleMaskFormatter data = new SimpleMaskFormatter("NN/NN/NNNN");
            MaskTextWatcher stw = new MaskTextWatcher(dataNasc, data);
            dataNasc.addTextChangedListener(stw);
            //Fim da mascara da data

            //Atualizar data de nascimento
            dataNasc.setText(data_nasc.getText().toString());
       dialogButtomAtualizar_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metodoBanco.atualizarInformacoes(email.getText().toString(), nome.getText().toString(), dataNasc.getText().toString()
                        , telefone.getText().toString(), view.getContext());
                alertDialog.dismiss();
            }
        });
        }

        if (requestCode == 3){
            //Criando a maskara para o campo cadastro de celular
            SimpleMaskFormatter cell = new SimpleMaskFormatter("(NN)NNNNN-NNNN");
            MaskTextWatcher mtw = new MaskTextWatcher(telefoneNovo, cell);
            telefoneNovo.addTextChangedListener(mtw);
            //Fim da mascara do telefone

            //Atualizar Telefone
            telefoneNovo.setText(telefone.getText().toString());
            dialogButtomAtualizar_telefone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    metodoBanco.atualizarInformacoes(email.getText().toString(), nome.getText().toString(), data_nasc.getText().toString()
                            , telefoneNovo.getText().toString(), view.getContext());
                    alertDialog.dismiss();
                }
            });
        }
        dialogButtonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Desabilitando o dialog
                alertDialog.dismiss();
            }
        });
    }

        // Metodo do custom dialog.
    public void showAlertDialogSair(int layoutDialog) {
        builderDialog = new AlertDialog.Builder(view.getContext());
        View LayoutView = getLayoutInflater().inflate(layoutDialog, null);
        AppCompatButton dialogButtomSair = LayoutView.findViewById(R.id.botao_sair_dialog);
        AppCompatButton dialogButtonCancelar = LayoutView.findViewById(R.id.botao_cancelar_dialog);
        builderDialog.setView(LayoutView);
        alertDialog = builderDialog.create();
        alertDialog.show();

        // Quando clicado no botão de "Ok" no custom dialog
        dialogButtomSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Desabilitando o dialog
                //Deslogando da conta e indo para o Login
                metodoBanco.deslogarApp(getActivity(), view.getContext());
                alertDialog.dismiss();
            }
        });
        dialogButtonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Desabilitando o dialog
                alertDialog.dismiss();
            }
        });
    };
}
package com.example.tcc_sgd;

import static android.content.Context.LOCATION_SERVICE;

//import static com.example.tcc_sgd.R.id.barra_pesquisa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsFragment extends Fragment {

    private View view;
    private EditText editTextPesquisa;
    private FloatingActionButton BotaoFeedback, BotaoInformacao;
    private SeekBar seekBar2;
    private TextView textViewMovimentacao, textViewNomeInformacao, textViewEnderecoInformaco,
            textViewTamanhoInformacao, textViewNomeFeedBack, textViewEnderecoFeedBack, textViewHora,
            textViewMovientoInfo, tituloProximo, textViewProgues, textViewMovimentoAtual, textViewMediaHoje, textViewFeedAnalisados;
    private RadioGroup radioGroupEstabelecimento, radioGroupTipo;
    private Estabelecimento estabelecimento;
    private int numeroMovimentacao, radioId2;
    private RadioButton radioButtonPequeno, radioButtonMedio, radioButtonGrande, radioButtonShop, radioButtonMercado, radioButtonRestaurante;
    private ImageView imageViewLocalizacao, imageViewIconEstabelecimentoInfo, imageViewDenuncia, imageViewAtualizar, imageViewDuvidaTamanho;
    private String tamanhoEstabelecimento, tipoEstabelecimento;
    private LinearLayout linearLayoutSeekBar, linearLayoutSeekBarAtual;
    private CircleImageView imagemViewMenu;

    //ATRIBUTOS UTILIZADOS PARA OS METODOS DO BANCO
    private BancoFirestore metodoBanco = new BancoFirestore();
    private FirebaseFirestore feed = FirebaseFirestore.getInstance();
    private final int[] contador = {0};
    private final String[] movimentacao = new String[3];
    private final int [] movimentacaoNumero = new int[2];

    //CRIANDO CAMPO DE LOADING
    private ProgressDialog progressDialog;

    //TEXT
    private TextView email, nome;
    private String usuarioID, nomeMenu, emailMenu;

    private AlertDialog.Builder builderDialog, builderDialog2 ;
    private AlertDialog alertDialog, alertDialogDenuncia ;

    // A classe FusedLocationProviderCliente irá fornecer os métodos para interagir com o GPS
    private FusedLocationProviderClient servicoLocalizacao;

    // Referenciando o Mapa que será montado na tela, mas tambem podemos
    //usar os métodos para posicionar, adicionar marcador e entre outros.
    private GoogleMap mMap;

    // Variáveis para armazenar os pontos recuperados pelo GPS do celular do usuario
    private double latitude, longitude;

    // Variável para armazenar se o usuario permitiu ou nao o uso do GPS
    private boolean permitiuGPS = false;

    // Variável para armazenar o ponto retornoado pelo GPS do celular do usuario.
    private Location ultimaPosicao;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        //Criando campo de laoding
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Carregando...");

        editTextPesquisa = view.findViewById(R.id.campo_pesquisa);
        // Inicializando o Google Places
        Places.initialize(view.getContext().getApplicationContext(), "AIzaSyDm2buo0TV0GNdmCvA0HPas-ojkn6in2jk");
        editTextPesquisa.setFocusable(false);
        editTextPesquisa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicializando a lista do Google Places
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS
                        , Place.Field.LAT_LNG, Place.Field.NAME);
                // Criando uma intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fieldList).setCountry("BR").setTypeFilter(TypeFilter.ESTABLISHMENT).build(view.getContext());
                // Startando a activity
                startActivityForResult(intent, 100);
            }
        });
        // Chamando o serviço de localização do Andrdoid e atribuindo ao nosso objeto
        servicoLocalizacao = LocationServices.getFusedLocationProviderClient(view.getContext());

        // Verificando se o  usuário já deu permissão para o uso do GPS.
        // Quando o usuário clicar para permitir ou não o uso e acesso aos dados de localização,
        // será executado o método onRequestPermissionsResults.
        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},120);
        }else{
            permitiuGPS = true;
        }
        // Recuperação do gerenciador de localização
        LocationManager gpsHabilitado = (LocationManager)getContext().getSystemService(LOCATION_SERVICE);
        // Verificando se o GPS está habilitado.
        if(!gpsHabilitado.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //... abrindo a tela de configurações para o usuario habilitar ou nao o GPS.
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(view.getContext(), "Para este aplicativo é necessário habilitar o GPS", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            // Se deu certo, inicializamos o place

            Place place = Autocomplete.getPlaceFromIntent(data);
            estabelecimento = new Estabelecimento(place.getName(), place.getAddress(),"horaFeedback");

            // Adicionando o marcador no local pesquisado
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            metodoBootomShetInformacao();

        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            // Se nao deu certo, inicializamos o status
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(view.getContext(), status.getStatusMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        // Quando o mapa estiver na tela do celular do usuario, o metodo
        // abaixo sera iniciado.
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            MapStyleOptions estiloMapa = MapStyleOptions.loadRawResourceStyle(view.getContext(), R.raw.style_mapa);
            googleMap.setMapStyle(estiloMapa); //COLOCANDO O ESTILO DO MAPA
            recuperarPosicaoAtual();
            metodoBotoes();
            adicionaComponentesVisuais();
        }
    };

    // Adicionando o botão para centralizar o mapa na posição atual.
    private void adicionaComponentesVisuais() {
        // Caso o objeto do mapa não existir, cancelamos o return.
        if (mMap == null) {
            return;
        }
        try {
            // Verificando se o usuário permitiu o acesso ao GPS, caso ele permitiu
            if (permitiuGPS) {
                // Adicionando o botão que quando o usuario clicar,
                // irá para a posição atual do seu aparelho/GPS.
                mMap.setMyLocationEnabled(true);
                // Habilitando o botão.
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else { // Caso o usuário não permitiu o acesso ao GPS.
                mMap.setMyLocationEnabled(false); // Removendo o botão.
                mMap.getUiSettings().setMyLocationButtonEnabled(false); // Desabilitando o botão

                // Limpando a última posição.
                ultimaPosicao  = null;

                // Pedindo a permissão do acesso ao GPS novamente
                if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},120);
                }
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // O método recuperarPosicaoAtual irá receber todas as atualizações enviadas pelo GPS do
    // celular do usuario
    private void recuperarPosicaoAtual() {
        try {
            // Testando se o usuario permitiu o uso dos dados de localização do seu dispositivo.
            if (permitiuGPS) {
                Task locationResult = servicoLocalizacao.getLastLocation();
                // Quando os dados estiverem recuperados
                locationResult.addOnCompleteListener((Activity) view.getContext(), new OnCompleteListener() {
                    @Override
                    public void onComplete(Task task) {
                        if (task.isSuccessful()) {
                            // Recuperando os dados da localização da última posição
                            ultimaPosicao = (Location) task.getResult();

                            // Caso os dados forem um valor válido
                            if(ultimaPosicao != null){
                                // Movendo a câmera para o ponto recuperado e aplicando
                                // um Zoom de 15.
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(ultimaPosicao.getLatitude(),
                                                ultimaPosicao.getLongitude()), 15));
                            }
                        } else {
                            // Exibindo um Toast se o valor do GPS não for válido
                            Toast.makeText(view.getContext().getApplicationContext(), "Não foi possível recuperar a posição.", Toast.LENGTH_LONG).show();
                            Log.e("TESTE_GPS", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("TESTE_GPS", e.getMessage());
        }
    }

    private void metodoBotoes(){
        // Metodo do botão da informação
        metodoBootomShetEtapa1();
        // Metodo do botão da informação do estabelecimento
        BotaoInformacao = view.findViewById(R.id.buttonInformacao);
        BotaoInformacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metodoBootomShetInformacao();
            }
        });
    }

    public void metodoBootomShetEtapa1(){
        //CRIANDO BOTAO SHETs
        final BottomSheetDialog bottomSheetDialogEtapa2 = new BottomSheetDialog(
                view.getContext(), R.style.BottomSheetDialogTheme
        );
        View bottomSheetView2 = LayoutInflater.from(view.getContext().getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet_etapa_2,
                        (LinearLayout) view.findViewById(R.id.bottomSheetContainer2)
                );
        //EVENTO DA SEEKBAR PARA MOSTRAR AO USUARIO (VAZIO, POUCO MOVIMENTADO...)
        seekBar2 = bottomSheetView2.findViewById(R.id.seekBar); //PEGANDO ID DA SEEKBAR PELO BOTTOMSHEET
        textViewMovimentacao = bottomSheetView2.findViewById(R.id.textViewMovimento);
        textViewProgues = bottomSheetView2.findViewById(R.id.textViewProgues);
        radioButtonPequeno = bottomSheetView2.findViewById(R.id.radioButtonPequeno);
        radioButtonMedio = bottomSheetView2.findViewById(R.id.radioButtonMedio);
        radioButtonGrande = bottomSheetView2.findViewById(R.id.radioButtonGrande);
        radioGroupEstabelecimento = bottomSheetView2.findViewById(R.id.radioGroupTamanho);

        //METODO DA IMAGEM DE DUVIDA
        imageViewDuvidaTamanho = bottomSheetView2.findViewById(R.id.duvidaTamanho);
        imageViewDuvidaTamanho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogDois(R.layout.dialog_informacao_tamanho);
            }
        });

        //EVENTO AO CLICAR NO RADIO BUTTON
        radioButtonPequeno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar2.setEnabled(true);
                seekBar2.setProgress(0);
                tamanhoEstabelecimento = "Pequeno";
            }
        });
        radioButtonMedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar2.setEnabled(true);
                seekBar2.setProgress(0);
                tamanhoEstabelecimento = "Médio";
            }
        });
        radioButtonGrande.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar2.setEnabled(true);
                seekBar2.setProgress(0);
                tamanhoEstabelecimento = "Grande";
            }
        });

        if (radioButtonPequeno.isChecked() || radioButtonMedio.isChecked() || radioButtonGrande.isChecked()) {
            seekBar2.setEnabled(true);
        }else {
            seekBar2.setEnabled(false);
            seekBar2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "Por favor Selecione o tamanho do estabelecimento", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // EVENTO LISTINER QUE "ESCUTA O MOVIMENTO" DA SEEK BAR
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // NECESSÁRIO ESTAR AQUI DENTRO NOVAMENTE POIS SE NÃO O LISTINER NÃO "EXERGA" O TEXTVIEW
                textViewProgues = bottomSheetView2.findViewById(R.id.textViewProgues);
                textViewMovimentacao = bottomSheetView2.findViewById(R.id.textViewMovimento);
                seekBar2 = bottomSheetView2.findViewById(R.id.seekBar); //PEGANDO ID DA SEEKBAR PELO BOTTOMSHEET
                seekBarMudarCor(progress);
                numeroMovimentacao = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //ACIONADO AO CLICAR NA SEEKBAR
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //ACIONADO AO "SOLTAR" A SEEKBAR
            }
        });

        //EVENTO DO BOTÃO QUE FICA DENTRO DO BOTTOM SHET
        bottomSheetView2.findViewById(R.id.buttonEnviar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metodoBanco.enviarFeedBack(estabelecimento, tamanhoEstabelecimento, numeroMovimentacao, tipoEstabelecimento, usuarioID, radioButtonPequeno,
                        radioButtonMedio, radioButtonGrande, bottomSheetDialogEtapa2, view, getActivity());
            }


        });

        //ETAPA 1 BOOTOM SHHET
        // Metodo do botão do feedback.
        BotaoFeedback = view.findViewById(R.id.ButtonFeedback);
        BotaoFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        view.getContext(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(view.getContext().getApplicationContext())
                        .inflate(
                                R.layout.layout_bottom_sheet,
                                (LinearLayout) view.findViewById(R.id.bottomSheetContainer)
                        );

                if(estabelecimento != null) {
                    tituloProximo = bottomSheetView2.findViewById(R.id.textViewTitulo);
                    textViewNomeFeedBack = bottomSheetView.findViewById(R.id.nomeEstabelecimentoFeedback);
                    textViewEnderecoFeedBack = bottomSheetView.findViewById(R.id.enderecoEstabelecimento);
                    radioButtonMercado = bottomSheetView.findViewById(R.id.radioButtonMercado);
                    radioButtonShop = bottomSheetView.findViewById(R.id.radioButtonShop);
                    radioButtonRestaurante = bottomSheetView.findViewById(R.id.radioButtonRestaurante);
                    imageViewDenuncia = bottomSheetView.findViewById(R.id.botaoDenuncia);
                    imageViewAtualizar = bottomSheetView.findViewById(R.id.botaoAtualizar);

                    //SETANDO NOME E ENDEREÇO DOS ESTABELECIMENTOS PESQUISADOS
                    textViewNomeFeedBack.setText(estabelecimento.getNome());
                    textViewEnderecoFeedBack.setText(estabelecimento.getEndereco());
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();

                    Button proximo = bottomSheetView.findViewById(R.id.buttonProximo);
                    proximo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(radioButtonMercado.isChecked() || radioButtonShop.isChecked() || radioButtonRestaurante.isChecked()){
                                //PEGANDO ID DO RADIO NUTTON SELECIONADO
                                radioGroupTipo = bottomSheetView.findViewById(R.id.radioGroupTipo);
                                radioId2 = radioGroupTipo.getCheckedRadioButtonId();

                                //SETANDO TEXTO DO TIPO DE ESTABELECIMENTO
                                switch (radioId2){
                                    case  R.id.radioButtonRestaurante:
                                        tipoEstabelecimento = "Restaurante";
                                        break;
                                    case R.id.radioButtonMercado:
                                        tipoEstabelecimento = "Mercado";
                                        break;
                                    case R.id.radioButtonShop:
                                        tipoEstabelecimento = "Shopping";
                                        break;
                                }

                                tituloProximo.setText("Tipo: " +tipoEstabelecimento);
                                bottomSheetDialog.cancel(); //FECHANDO BUTTOM SHEET
                                bottomSheetDialogEtapa2.setContentView(bottomSheetView2);
                                bottomSheetDialogEtapa2.show(); //ABRINDO SEGUNDA ETAPA
                            } else {
                                Toast.makeText(bottomSheetView.getContext(), "Por favor selecione o tipo de estabelecimento!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(view.getContext(), "Por favor pesquise um estabelecimento antes de clicar no botão de FeedBack!", Toast.LENGTH_SHORT).show();
                }
                //METODO DA IMAGEM DE DUVIDA
                imageViewLocalizacao = bottomSheetView.findViewById(R.id.imagemDuvida);
                imageViewLocalizacao.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialogDois(R.layout.dialog_informacao_estabelecimento);
                    }
                });

            }
        });
    }

    public void metodoBootomShetInformacao(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                view.getContext(), R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(view.getContext().getApplicationContext())
                .inflate(
                        R.layout.layout_informacao_estabelecimento,
                        (LinearLayout) view.findViewById(R.id.bottomSheetContainer)
                );

        if(estabelecimento != null) {
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
            progressDialog.show();
            pesquisarMovimento(bottomSheetDialog, progressDialog);

            //METODO DO BOTÃO DE ATUALIZAR
            imageViewAtualizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.cancel();
                    pesquisarMovimento(bottomSheetDialog, progressDialog);
                    bottomSheetDialog.show();
                }
            });

            //METODO DO BOTÃO DE DENUNCIA
            imageViewDenuncia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialogDenuncia(R.layout.dialog_denuncia_informacao);
                }
            });

        } else Toast.makeText(view.getContext(), "Por favor pesquise um estabelecimento antes de clicar no botão de informações!", Toast.LENGTH_SHORT).show();
    }

    public void pesquisarMovimento(BottomSheetDialog bottomSheetView, ProgressDialog progressDialog){
        //PEGANDO IDS
        textViewMovimentacao = bottomSheetView.findViewById(R.id.textViewMovimentoInfo);
        textViewNomeInformacao = bottomSheetView.findViewById(R.id.nomeEstabelecimentoInfo);
        textViewEnderecoInformaco = bottomSheetView.findViewById(R.id.enderecoEstabelecimentoInfo);
        textViewTamanhoInformacao = bottomSheetView.findViewById(R.id.tamanhoEstabelecimentoInfo);
        textViewHora = bottomSheetView.findViewById(R.id.textViewVistoFeedBack);
        textViewMovimentoAtual = bottomSheetView.findViewById(R.id.textViewMovimentoAtual);
        textViewMovientoInfo = bottomSheetView.findViewById(R.id.textViewMovimentoInfo);
        textViewFeedAnalisados = bottomSheetView.findViewById(R.id.textViewNumeroDeFeedBacks);
        textViewMediaHoje = bottomSheetView.findViewById(R.id.textViewMediaHoje);
        imageViewAtualizar = bottomSheetView.findViewById(R.id.botaoAtualizar);
        imageViewDenuncia = bottomSheetView.findViewById(R.id.botaoDenuncia);
        linearLayoutSeekBar = bottomSheetView.findViewById(R.id.seek_bar_informacao);
        linearLayoutSeekBarAtual = bottomSheetView.findViewById(R.id.seek_bar_inf_atual);
        imageViewIconEstabelecimentoInfo = bottomSheetView.findViewById(R.id.iconEstabelecimentoInfo);

        metodoBanco.confereDiaEHora(view.getContext());
        metodoBanco.pesquisarMovimento(estabelecimento, textViewMediaHoje, textViewNomeInformacao, textViewEnderecoInformaco, textViewTamanhoInformacao,
                textViewHora, textViewFeedAnalisados, textViewMovimentoAtual, textViewMovientoInfo, linearLayoutSeekBarAtual, linearLayoutSeekBar, imageViewIconEstabelecimentoInfo,
                contador, movimentacaoNumero, movimentacao, view, bottomSheetView, progressDialog);

    }

    public void seekBarMudarCor(int progress){
        switch (tipoEstabelecimento) {
            case "Restaurante":
                //ESCALA PARA RESTAURANTE PEQUENO
                if (radioButtonPequeno.isChecked()) {
                    seekBar2.setMax(100);

                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 30) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 30 && progress <= 75) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 75) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonMedio.isChecked()) {
                    //ESCALA RESTAURANTE MEDIO...
                    seekBar2.setMax(200);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 60) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 60 && progress <= 150) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 150) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonGrande.isChecked()) {
                    //ESCALA RESTAURANTE GRANDE...
                    seekBar2.setMax(400);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 120) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 120 && progress <= 300) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 300) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                break;
            case "Shopping":
                //ESCALA PARA RESTAURANTE PEQUENO
                if (radioButtonPequeno.isChecked()) {
                    seekBar2.setMax(4000);
                    //ESCALA SHOPING PEQUENO
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 1200) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 1200 && progress <= 3000) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 3000) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonMedio.isChecked()) {
                    //ESCALA SHOPING MEDIO
                    seekBar2.setMax(6000);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 1800) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 1800 && progress <= 4500) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 4500) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonGrande.isChecked()) {
                    //ESCALA SHOPING GRANDE
                    seekBar2.setMax(15000);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 4500) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 4500 && progress <= 11250) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 11250) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }

                break;
            case "Mercado":
                if (radioButtonPequeno.isChecked()) {
                    seekBar2.setMax(500);

                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 150) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 150 && progress <= 375) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 375) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonMedio.isChecked()) {
                    //ESCALA RESTAURANTE MEDIO...
                    seekBar2.setMax(1500);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 450) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 450 && progress <= 1125) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 1125) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }
                if (radioButtonGrande.isChecked()) {
                    //ESCALA RESTAURANTE GRANDE...
                    seekBar2.setMax(3500);
                    if (progress == 0) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Vazio");
                        textViewMovimentacao.setTextColor(Color.parseColor("#000000")); //MUDANDO COR DO TEXTO
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY); //MUDANDO COR DA SEEKBAR
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); //MUDANDO COR DO PONTEIRO DA SEEKBAR
                    }
                    if (progress > 0 && progress <= 1050) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#008000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimento Tranquilo");
                        textViewMovimentacao.setTextColor(Color.parseColor("#008000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#008000"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 1050 && progress <= 2625) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FFFF00")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Movimentado");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FFFF00"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN);
                    }
                    if (progress > 2625) {
                        textViewProgues.setText(progress + " Pessoas aprox.");
                        textViewProgues.setTextColor(Color.parseColor("#FF0000")); //MUDANDO COR DO TEXTO
                        textViewMovimentacao.setText("Muito movimentado!");
                        textViewMovimentacao.setTextColor(Color.parseColor("#FF0000"));
                        seekBar2.getProgressDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
                        seekBar2.getThumb().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN);
                    }
                }

                break;
        }
    }

    private void showAlertDialogDenuncia(int dialog_informacao_denuncia) {
        builderDialog2 = new AlertDialog.Builder(view.getContext());
        View LayoutView = getLayoutInflater().inflate(dialog_informacao_denuncia, null);
        AppCompatButton dialogButtomEnviar = LayoutView.findViewById(R.id.botao_enviar_dialog);
        AppCompatButton dialogButtomCancelar = LayoutView.findViewById(R.id.botao_cancelar_denuncia);
        builderDialog2.setView(LayoutView);

        alertDialogDenuncia = builderDialog2.create();
        alertDialogDenuncia.show();

        // Quando clicado no botão de "Ok" no custom dialog
        dialogButtomEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Criando objeto para chamar o metodo de enviar denuncia
                EnviarDenuncia enviarDenuncia = new EnviarDenuncia("tipoProblema", "idUsuario", "nomeEstabelecimento", "enderecoEstabelecimento","textoDescricao" );
                enviarDenuncia.enviarDenuncia(LayoutView, usuarioID, estabelecimento.getNome() , estabelecimento.getEndereco(),getContext());
                alertDialogDenuncia.cancel();
            }
        });
        //Metdo do botão de cancelar
        dialogButtomCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogDenuncia.dismiss();
            }
        });
    }

    private void showAlertDialogDois(int dialog_informacao_estabelecimento) {
        builderDialog = new AlertDialog.Builder(view.getContext());
        View LayoutView = getLayoutInflater().inflate(dialog_informacao_estabelecimento, null);
        AppCompatButton dialogButtom = LayoutView.findViewById(R.id.botao_ok_dialog);
        builderDialog.setView(LayoutView);
        alertDialog = builderDialog.create();
        alertDialog.show();

        // Quando clicado no botão de "Ok" no custom dialog
        dialogButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Desabilitando o dialog
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioLogado = FirebaseAuth.getInstance().getCurrentUser();
        //METODO PARA VERIFICAR SE  O USUARIO ESTA LOGADO
        if (usuarioLogado != null){
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            DocumentReference documentReference = feed.collection("Usuarios").document(usuarioID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    //INSERINDO NOME DO USUARIO LOGADO NO MENU
                    if(documentSnapshot != null){
                        try {
                            nome = getActivity().findViewById(R.id.nome_Usuario);
                            email = getActivity().findViewById(R.id.textViewEmailUsuario);
                            nomeMenu =  documentSnapshot.getString("nome");
                            emailMenu = documentSnapshot.getString("email");
                            nome.setText(nomeMenu);
                            email.setText(emailMenu);

                            imagemViewMenu = getActivity().findViewById(R.id.imageViewMenu);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(user != null){
                                if(user.getPhotoUrl() != null){
                                    Glide.with(getContext())
                                            .load(user.getPhotoUrl())
                                            .into(imagemViewMenu);
                                } else imagemViewMenu.setImageResource(R.drawable.sgdbottomsheet);
                            }
                        } catch (Exception e){
                            onStart();
                        }
                    }
                }
            });
        } else getActivity().finish();
    }
}

package com.baadalletta.helper.network;

import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseHelper;
import com.baadalletta.helper.models.ResponseKategori;
import com.baadalletta.helper.models.ResponseLogin;
import com.baadalletta.helper.models.ResponsePembelian;
import com.baadalletta.helper.models.ResponseProduk;
import com.baadalletta.helper.models.ResponseStok;
import com.baadalletta.helper.models.ResponseStokAll;
import com.baadalletta.helper.models.ResponseTambahPembelian;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    //GET ALL PRODUK
    @GET("helpers/produk/getAll")
    Call<ResponseProduk> getAllProduk();

    // DELETE PRODUK
    @DELETE("helpers/produk/delete/{produk_id}")
    Call<ResponseApi> deleteProduk(@Path("produk_id") String produk_id);

    // ADD PRODUK
    @Multipart
    @POST("helpers/produk/post")
    Call<ResponseApi> addProduk(@Part("kategori_produk") RequestBody kategori_produk,
                                @Part("nama") RequestBody nama,
                                @Part("harga") RequestBody harga,
                                @Part("panjang") RequestBody panjang,
                                @Part("lebar") RequestBody lebar,
                                @Part("volume") RequestBody volume,
                                @Part MultipartBody.Part foto);

    // UPDATE PRODUK NO FILE
    @POST("helpers/produk/update/{produk_id}")
    Call<ResponseApi> updateProdukNoFile(@Path("produk_id") String produk_id,
                                         @Query("kategori_produk") String kategori_produk,
                                         @Query("nama") String nama,
                                         @Query("harga") String harga,
                                         @Query("panjang") String panjang,
                                         @Query("lebar") String lebar,
                                         @Query("volume") String volume);

    // UPDATE PRODUK
    @Multipart
    @POST("helpers/produk/update/{produk_id}")
    Call<ResponseApi> updateProduk(@Path("produk_id") String produk_id,
                                   @Part("kategori_produk") RequestBody kategori_produk,
                                   @Part("nama") RequestBody nama,
                                   @Part("harga") RequestBody harga,
                                   @Part("panjang") RequestBody panjang,
                                   @Part("lebar") RequestBody lebar,
                                   @Part("volume") RequestBody volume,
                                   @Part MultipartBody.Part foto);

    // UPDATE PRODUK VIDEO
    @Multipart
    @POST("helpers/produk/update/{produk_id}")
    Call<ResponseApi> updateProdukVideo(@Path("produk_id") String produk_id,
                                        @Part("kategori_produk") RequestBody kategori_produk,
                                        @Part("nama") RequestBody nama,
                                        @Part("harga") RequestBody harga,
                                        @Part("panjang") RequestBody panjang,
                                        @Part("lebar") RequestBody lebar,
                                        @Part("volume") RequestBody volume,
                                        @Part MultipartBody.Part video);

    // UPDATE PRODUK WITH VIDEO
    @Multipart
    @POST("helpers/produk/update/{produk_id}")
    Call<ResponseApi> updateProdukWithVideo(@Path("produk_id") String produk_id,
                                            @Part("kategori_produk") RequestBody kategori_produk,
                                            @Part("nama") RequestBody nama,
                                            @Part("harga") RequestBody harga,
                                            @Part("panjang") RequestBody panjang,
                                            @Part("lebar") RequestBody lebar,
                                            @Part("volume") RequestBody volume,
                                            @Part MultipartBody.Part foto,
                                            @Part MultipartBody.Part video);

    // ADD PRODUK WITH VIDEO
    @Multipart
    @POST("helpers/produk/post")
    Call<ResponseApi> addProdukWithVideo(@Part("kategori_produk") RequestBody kategori_produk,
                                         @Part("nama") RequestBody nama,
                                         @Part("harga") RequestBody harga,
                                         @Part("panjang") RequestBody panjang,
                                         @Part("lebar") RequestBody lebar,
                                         @Part("volume") RequestBody volume,
                                         @Part MultipartBody.Part foto,
                                         @Part MultipartBody.Part video);


    //GET ALL KATEGORI
    @GET("helpers/kategori/getAll")
    Call<ResponseKategori> getAllkategori();

    // ADD KATEGORI
    @POST("helpers/kategori/post")
    Call<ResponseApi> addKategori(@Query("nama") String nama);

    // DELETE KATEGORI
    @DELETE("helpers/kategori/delete/{kategori_id}")
    Call<ResponseApi> deleteKategori(@Path("kategori_id") String kategori_id);

    // UPDATE KATEGORI
    @POST("helpers/kategori/update/{kategori_id}")
    Call<ResponseApi> updateKategori(@Path("kategori_id") String kategori_id,
                                     @Query("nama") String nama);

    // LOGIN
    @POST("helpers/auth")
    Call<ResponseLogin> postLogin(@Query("username") String username,
                                  @Query("password") String password);

    // HELPER
    @GET("helpers/helpersById/{helper_id}")
    Call<ResponseHelper> getHelperId(@Path("helper_id") String helper_id);


    // UPDATE PASSWORD HELPER
    @POST("helpers/update/ubah-password/{helper_id}")
    Call<ResponseApi> updatePasswordHelper(@Path("helper_id") String helper_id,
                                           @Query("password_lama") String password_lama,
                                           @Query("password_baru") String password_baru);

    // UPDATE FOTO HELPER
    @Multipart
    @POST("helpers/update/file-foto/{helper_id}")
    Call<ResponseApi> updateFotoHelper(@Path("helper_id") String helper_id,
                                       @Part MultipartBody.Part foto);


    // GET STOK ID PRODUK
    @GET("helpers/produk/stok/{produk_id}")
    Call<ResponseStok> getStokProdukId(@Path("produk_id") String produk_id);

    // GET STOK ID PRODUK
    @GET("helpers/produk/stok")
    Call<ResponseStokAll> getStokAll();

    // UPDATE STOK ID STOK
    @POST("helpers/produk/stok/update/{stok_id}")
    Call<ResponseStok> updateStokId(@Path("stok_id") String stok_id,
                                    @Query("jumlah") String jumlah);


    // GET DATA PEMBELIAN
    @GET("helpers/harga-pembelian/list/{helper_id}")
    Call<ResponsePembelian> getDataPembelian(@Path("helper_id") String helper_id);

    // ADD PEMBELIAN PRODUK
    @Multipart
    @POST("helpers/harga-pembelian/store")
    Call<ResponseTambahPembelian> tambahPembelian(@Part("id_helper") RequestBody id_helper,
                                                  @Part("id_produk") RequestBody id_produk,
                                                  @Part("harga") RequestBody harga,
                                                  @Part MultipartBody.Part foto);


}

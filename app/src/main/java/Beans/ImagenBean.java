package Beans;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by diego on 1/07/2016.
 */
public class ImagenBean implements Parcelable {

    public Bitmap bitmapImage;
    public int idImagen;
    public int indexImagen;
    public String rutaLocalImagen;

    public ImagenBean() {

    }

    protected ImagenBean(Parcel in) {
        this.bitmapImage = in.readParcelable(null);
        this.idImagen = in.readInt();
        this.indexImagen = in.readInt();
        this.rutaLocalImagen = in.readString();
    }

    public static final Creator<ImagenBean> CREATOR = new Creator<ImagenBean>() {
        @Override
        public ImagenBean createFromParcel(Parcel in) {
            return new ImagenBean(in);
        }

        @Override
        public ImagenBean[] newArray(int size) {
            return new ImagenBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmapImage, flags);
        dest.writeInt(idImagen);
        dest.writeInt(indexImagen);
        dest.writeString(rutaLocalImagen);
    }
}
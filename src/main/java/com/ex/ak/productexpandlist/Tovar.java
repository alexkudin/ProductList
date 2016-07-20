package com.ex.ak.productexpandlist;

import java.io.Serializable;

class	Tovar implements Serializable
{
    // ----- Class constants -----------------------------------------------
    /**
     * Category Fruits
     */
    public final	static	int	CATEGORY_FRUITS		= 0;
    /**
     * Category Chocos
     */
    public final	static	int	CATEGORY_CHOCO		= 1;
    /**
     * Category Beverages
     */
    public final	static	int	CATEGORY_BEVERAGES	= 2;

    // ----- Class members -------------------------------------------------
    public					String		name;
    public					double		price;
    public 					int			weight;
    public 					int			idCategory;

    // ----- Class methods -------------------------------------------------
    public Tovar(String name, double price, int weight, int idCat)
    {
        this.name		= name;
        this.price		= price;
        this.weight		= weight;
        this.idCategory	= idCat;
    }

    public Tovar	cloneTovar()
    {
        return	new Tovar(this.name, this.price, this.weight, this.idCategory);
    }
    public String toString()
    {
        return this.name + "|" + this.price + "^" + this.weight + "!" + this.idCategory + "\r\n";
    }
}

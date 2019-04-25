package Processing;

import com.nordicsemi.nrfUARTv2.Utils;

import android.util.Log;

public class CalHeartRate {
	int peakcount = 0;
	int Rtc=0;
	/**
	Calculate Heart Rate
*/
public int XoaTaiViTri (int a[], int n, int vitri)
{
	int i;
	for ( i = vitri; i < n-1 ; i++)
	a[i] = a[i+1];
	n--;
	return n;
}

public int max_find(double []buff,int []temp)
{
	int i;
	int	max_index=0;
	int	max_peak=temp[max_index];
	for( i = 0;i <75; i++)
	{
		//Log.d(Utils.MTAG, "i="+i+", [temp[i]="+temp[i]+", temp[max_index]="+temp[max_index]+", max_index="+max_index);
		if(buff[temp[i]] > buff[temp[max_index]])
		{
		max_index = i;
		max_peak = temp[max_index];

		}
	}
	return max_peak;
}



public int peak_max_find(double []x,int size,int []peak,int start, int peakcount, int count)
{
	int pos,i,j;
	int startIndex = 0;
	//Log.d(Utils.MTAG, "peakcount-1="+(peakcount-1)+", peak.lenght="+peak.length+",size="+size+"count-size="+(count-size));
	if(peakcount>0)
		startIndex = peak[peakcount-1] + size;
	else
		startIndex = size;

	for( i = startIndex; i<(count-size); i++)
	{
		pos=i;	
		for ( j=-size; j < size; j++)
		{	
			if(x[i+j] > x[i]||x[i] == x[i+1])
			{
			 pos=0;
			 break;
			}
		
		}
		
		if(pos!=0)
		{
			peak[peakcount++]=pos;
		}
	}
	return peakcount;
}

public double []binhphuong_viphan(double []x,int count)
{
	int i;
	double y[] = new double[count - 5];
	for( i = 0; i < count-5; i++)
	{
		y[i]=(x[i+5]-x[i])*(x[i+5]-x[i]);

	}
	return y;

}

/**
 Find QS
*/
public void FindQS(int [] peak,double [] buff,int count,int [] Q,int [] S,int window)
{
int i = 0, j = 0;
int flag=0;
int point =0;
	for ( i = 0; i < peakcount; i++)
	{	point = peak[i];
		while(true)
		{
			flag=1;
			for( j = 1;j <= window; j++)
			{
				if((buff[point] > buff[point+j])||(buff[point]>buff[point-j]))
				{
					point--;
					flag=0;
					break;
				}
			}
				if(flag==1)
				{
				Q[i]=point;
				break;
				}
		}
		point = peak[i];
		while(true)
		{
			flag = 1;
			for( j=1; j <= window; j++)
			{
				if((buff[point] > buff[point+j])||(buff[point] > buff[point-j]))
				{
					point++;
					flag=0;
					break;
				}
			}
				if(flag==1)
				{
				S[i]=point;
				break;
				}
		}	
	}
}

//---------------////

public double HR(double []buff,int []peak,int count)
{
	double heartRate;
	int size=100;
	int i,j;
	double trungbinh=0;
	double []diff = binhphuong_viphan(buff,count);
	peakcount = peak_max_find(diff,size,peak,peak[peakcount], peakcount, count - 5);
	if(peakcount>0){
	
		for( i=0; i < peakcount; i++)
		{
			if(i == peakcount) 
			break;
			if(diff[peak[i]] > 300000)
			{
				peakcount = XoaTaiViTri(peak, peakcount, i);
				i--;
			}
		}
		
		
		
		for( i = 0; i< peakcount; i++)
		{
			trungbinh = trungbinh+diff[peak[i]];
		
		}
		trungbinh = trungbinh/peakcount;
		for( i=0; i < peakcount; i++)
		{
			if(i == peakcount) 
			break;
			if(diff[peak[i]] > (4*trungbinh))
			{
				peakcount = XoaTaiViTri(peak, peakcount, i);
				i--;
			}
		}
		trungbinh=0;
		for(i = 0; i < peakcount; i++)
 		{
			trungbinh = trungbinh+diff[peak[i]];
 		}
 		trungbinh = trungbinh/peakcount;
 		for(i=0; i < peakcount; i++)
 		{
			if(i == peakcount) break;
			if(diff[peak[i]] < (0.7*trungbinh))
			{
			
				peakcount = XoaTaiViTri(peak,peakcount,i);
				
				i--;

			}
 		}
 		
 		for(i=0; i < peakcount; i++)
 		{
			if(i == peakcount) break;
			if(peak[i]==0)
			{
			
				peakcount = XoaTaiViTri(peak,peakcount,i);
				
				i--;

			}
 		}
		//int * time_distance = (int*) malloc(sizeof(int)*(*peakcount-1));
		if(peakcount<5)
			return 0;
		int RR_count=0;
		int RR[] = new int[peakcount];
		boolean test=false;
		
		for(i=0;i<peakcount-1;i++)
		{
		RR[i]=peak[i+1]-peak[i];
		RR_count++;
		}
		for(i=0;i<RR_count-4;i++)
		{	test=true;
			for (j=1;j<=3;j++)
			{
				if(  (RR[i+j]<(0.7*RR[i]) )  ||( RR[i+j] >  (1.3*RR[i]) ) )
					{	test=false;
						break;
					}
			}
			if(test)
			{
			Rtc=(RR[i]+RR[i+1]+RR[i+2]+RR[i+3])/4;
			}
		}
		
		if(Rtc==0) return 0;
		for (i=0;i<peakcount;i++)
		{
			if(i==peakcount) break;
			if( (peak[i+1]-peak[i])<0.6*Rtc)
			{
				peakcount = XoaTaiViTri(peak,peakcount,i+1);	
				i--;
			}
		}
		double	distance_count=1;
		double	distance_sum=peak[1]-peak[0];
		double	distance_trungbinh=distance_sum;
		for( i=0;i < peakcount - 1;i++)
		{	
			distance_trungbinh = distance_sum/distance_count;
			if(peak[i+1]-peak[i]<2*distance_trungbinh)
			{
			
				distance_sum = distance_sum+(peak[i+1]-peak[i]);
				distance_count++;
			}
			//Log.d(Utils.MTAG, "Peak["+i+"]="+peak[i]);
		}
		distance_trungbinh = distance_sum/distance_count;
////////TUAN 29/9/////////////
		//Log.d(Utils.MTAG, "distance="+distance+", distance_count="+distance_count+", distance_sum="+distance_sum+", distance_trungbinh"+distance_trungbinh);
 		heartRate=(60*500)/(distance_trungbinh);
 		//Log.d(Utils.MTAG, "peakcount "+peakcount);
 		return heartRate;
	}
	else
		return 0;
}
//-----------------//
public void TimR(double []buff,int []peak)
{
	int temp[] = new int[75];
	int i,j;
	for( i=0;i < peakcount;i++)
		{
			//Log.d(Utils.MTAG, "TimR1 peak["+i+"]="+peak[i]);
			for( j=-75/2;j<=75/2;j++)
			{
				temp[j+75/2]=peak[i]+j;
			}
			peak[i]=max_find(buff,temp);
			//Log.d(Utils.MTAG, "TimR2 peak["+i+"]="+peak[i]);

		}
 
}

//--------------//
public double [] daoham(double [] x,int count)
{
	int i;

	double [] y = new double[count-1];
	for( i=0;i<count-1;i++)
	{
		y[i]=(x[i+1]-x[i]);

	}
	return y;

}
public void loc10hz(double[] buff,int[] Q,int[] Tonset,double[] Hn_10hz,int lenHn_10hz)
{

	for(int i=0;i<peakcount-1;i++)

	{
		double BienDo=buff[Tonset[i]];

		int len_temp=Q[i+1] - Tonset[i]+1;
		double temp[]=new double[len_temp];
		for(int j=Tonset[i];j<=Q[i+1];j++)

		{
			temp[j-Tonset[i]]=buff[j]-BienDo;
		}
		double [] filted=Filter_50_100.conv(temp,len_temp,Hn_10hz,lenHn_10hz);
		for(int j=Tonset[i];j<=Q[i+1];j++)
		{
			buff[j]=filted[j-Tonset[i]+150]+BienDo;
		}

	}
}

public void FindQSTonset(int []peak,double []buff,int count,int []Q,int []S,int window,int []Tonset,double []daohambac2)
{
int flag=0;
int point =0;
	for (int i=0;i<peakcount;i++)
	{	  point=peak[i];
		boolean test=true;
		while(test)
		{flag=1;
			for(int j=1;j<=window;j++)
			{
				if((buff[point]>buff[point+j])||(buff[point]>buff[point-j]))
				{point--;
				flag=0;
				break;
				}
			}
				if(flag==1)
				{
				Q[i]=point;
				test=false;
				}
		}
		point=peak[i];
		test=true;
		while(test)
		{flag=1;
			for(int j=1;j<=window;j++)
			{
				if((buff[point]>buff[point+j])||(buff[point]>buff[point-j]))
				{point++;
				flag=0;
				break;
				}
			}
				if(flag==1)
				{
				S[i]=point;
				test=false;
				}
		}
		 point=S[i];
		 test=true;
    while(test)
	{
    if((daohambac2[point-1]<=0)&&(daohambac2[point+1]>=0))
        {Tonset[i]=point;
        test=false;
        break;
		}
    else
        point++;
	}
	}
}

public boolean isInverse(double buff[], int peak[], int Q[], int S[]) {
	int i;
	double	sum = 0,
			sumOffsetPeak = 0,
			sumOffsetQ = 0,
			sumOffsetS = 0,
			avg = 0,
			avgOffsetPeak = 0,
			avgOffsetQ = 0,
			avgOffsetS = 0;
	Log.d(Utils.MTAG, "Peakcount: " + peakcount);
	for(i=0; i<buff.length; i++) {
		sum += buff[i];
	}
	avg = sum/buff.length;
	
	for(i=0; i<peakcount; i++) {
		sumOffsetPeak += Math.abs(buff[peak[i]] - avg);
	}
	avgOffsetPeak = sumOffsetPeak/peakcount;
	
	for(i=0; i<peakcount; i++) {
		sumOffsetQ +=  Math.abs(buff[Q[i]] - avg);
	}
	avgOffsetQ = sumOffsetQ/peakcount;
	
	for(i=0; i<peakcount; i++) {
		sumOffsetS +=  Math.abs(buff[S[i]] - avg);
	}
	avgOffsetS = sumOffsetS/peakcount;
	
	if(avgOffsetPeak > avgOffsetQ && avgOffsetPeak > avgOffsetS) {
		return false;
	}
	return true;
}

public int getPeakCount() {
	return peakcount;
}

public static double Hn_10[] ={
-1.25023320567641*Math.pow(10,-19),
-2.14954971090868*Math.pow(10,-05),
-4.31024099779152*Math.pow(10,-05),
-6.46394319050838*Math.pow(10,-05),
-8.59138609597121*Math.pow(10,-05),
-0.000106718502743356,
-0.000126829066580441,
-0.000146002255027031,
-0.000163974730378486,
-0.000180463119163816,
-0.000195165186905419,
-0.000207762281335220,
-0.000217923103587748,
-0.000225308824574912,
-0.000229579518844779,
-0.000230401841893808,
-0.000227457830366631,
-0.000220454659109724,
-0.000209135145928216,
-0.000193288755392445,
-0.000172762818366640,
-0.000147473655218603,
-0.000117417268937985,
-8.26792605240968*Math.pow(10,-05),
-4.34436137200429*Math.pow(10,-05),
2.21335526688467*Math.pow(10,-19),
4.72507320129448*Math.pow(10,-05),
9.77931957020622*Math.pow(10,-05),
0.000150996514942755,
0.000206116021356137,
0.000262297789959442,
0.000318586145357320,
0.000373934206482134,
0.000427217468328144,
0.000477250345533527,
0.000522805526595393,
0.000562635910625722,
0.000595498822625910,
0.000620182130079154,
0.000635531815048285,
0.000640480493707285,
0.000634076321042491,
0.000615511673938374,
0.000584150972466743,
0.000539556978194286,
0.000481514900757198,
0.000410053650617663,
0.000325463597332123,
0.000228310229044668,
0.000119443160176601,
-4.84465367199609*Math.pow(10,-19),
-0.000128595325780650,
-0.000264640116217067,
-0.000406166511462097,
-0.000550961283625865,
-0.000696591780749093,
-0.000840437876187805,
-0.000979729646181074,
-0.00111159036814828,
-0.00123308430395113,
-0.00134126860863503,
-0.00143324858871818,
-0.00150623542756045,
-0.00155760540125414,
-0.00158495952921208,
-0.00158618254135752,
-0.00155950000045626,
-0.00150353239528243,
-0.00141734501923840,
-0.00130049247063647,
-0.00115305665556767,
-0.000975677242168470,
-0.000769573605743985,
-0.000536557416757193,
-0.000279035156841939,
8.43907413831576*Math.pow(10,-19),
0.000296987335158741,
0.000607828973423159,
0.000927931528415819,
0.00125225882046612,
0.00157539527514790,
0.00189161940295326,
0.00219498655562181,
0.00247941995632897,
0.00273880881308859,
0.00296711215109233,
0.00315846684389256,
0.00330729818871492,
0.00340843126085212,
0.00345720119877780,
0.00344956051765397,
0.00338218152614610,
0.00325255193125587,
0.00305906175903495,
0.00280107979577992,
0.00247901786426550,
0.00209438139178372,
0.00164980489966314,
0.00114907124540234,
0.000597113675871570,
-1.20334946046354*Math.pow(10,-18),
-0.000635101541712198,
-0.00129997484974247,
-0.00198542682486726,
-0.00268137605913406,
-0.00337695860280578,
-0.00406064975538034,
-0.00472040054199746,
-0.00534378726421759,
-0.00591817226118392,
-0.00643087378821475,
-0.00686934271925650,
-0.00722134361129699,
-0.00747513753625468,
-0.00761966399194530,
-0.00764471915079774,
-0.00754112769471422,
-0.00730090551782091,
-0.00691741065609189,
-0.00638547992347768,
-0.00570154889702110,
-0.00486375309656245,
-0.00387200844539121,
-0.00272806937329040,
-0.00143556322892805,
1.46647930097469*Math.pow(10,-18),
0.00157124330804204,
0.00326896391581018,
0.00508220003954127,
0.00699832270767639,
0.00900315176590514,
0.0110810948404694,
0.0132153076720726,
0.0153878738953464,
0.0175800020270652,
0.0197722371453818,
0.0219446844970338,
0.0240772420639529,
0.0261498389585868,
0.0281426764014334,
0.0300364679669633,
0.0318126757666749,
0.0334537392710922,
0.0349432935558524,
0.0362663738896243,
0.0374096037615959,
0.0383613636710715,
0.0391119382679742,
0.0396536397367314,
0.0399809056524805,
0.0400903699025880,
0.0399809056524805,
0.0396536397367314,
0.0391119382679742,
0.0383613636710715,
0.0374096037615959,
0.0362663738896243,
0.0349432935558524,
0.0334537392710922,
0.0318126757666749,
0.0300364679669633,
0.0281426764014334,
0.0261498389585868,
0.0240772420639529,
0.0219446844970338,
0.0197722371453818,
0.0175800020270652,
0.0153878738953464,
0.0132153076720726,
0.0110810948404694,
0.00900315176590514,
0.00699832270767639,
0.00508220003954127,
0.00326896391581018,
0.00157124330804204,
1.46647930097469*Math.pow(10,-18),
-0.00143556322892805,
-0.00272806937329040,
-0.00387200844539121,
-0.00486375309656245,
-0.00570154889702110,
-0.00638547992347768,
-0.00691741065609189,
-0.00730090551782091,
-0.00754112769471422,
-0.00764471915079774,
-0.00761966399194530,
-0.00747513753625468,
-0.00722134361129699,
-0.00686934271925650,
-0.00643087378821475,
-0.00591817226118392,
-0.00534378726421759,
-0.00472040054199746,
-0.00406064975538034,
-0.00337695860280578,
-0.00268137605913406,
-0.00198542682486726,
-0.00129997484974247,
-0.000635101541712198,
-1.20334946046354*Math.pow(10,-18),
0.000597113675871570,
0.00114907124540234,
0.00164980489966314,
0.00209438139178372,
0.00247901786426550,
0.00280107979577992,
0.00305906175903495,
0.00325255193125587,
0.00338218152614610,
0.00344956051765397,
0.00345720119877780,
0.00340843126085212,
0.00330729818871492,
0.00315846684389256,
0.00296711215109233,
0.00273880881308859,
0.00247941995632897,
0.00219498655562181,
0.00189161940295326,
0.00157539527514790,
0.00125225882046612,
0.000927931528415819,
0.000607828973423159,
0.000296987335158741,
8.43907413831576*Math.pow(10,-19),
-0.000279035156841939,
-0.000536557416757193,
-0.000769573605743985,
-0.000975677242168470,
-0.00115305665556767,
-0.00130049247063647,
-0.00141734501923840,
-0.00150353239528243,
-0.00155950000045626,
-0.00158618254135752,
-0.00158495952921208,
-0.00155760540125414,
-0.00150623542756045,
-0.00143324858871818,
-0.00134126860863503,
-0.00123308430395113,
-0.00111159036814828,
-0.000979729646181074,
-0.000840437876187805,
-0.000696591780749093,
-0.000550961283625865,
-0.000406166511462097,
-0.000264640116217067,
-0.000128595325780650,
-4.84465367199609*Math.pow(10,-19),
0.000119443160176601,
0.000228310229044668,
0.000325463597332123,
0.000410053650617663,
0.000481514900757198,
0.000539556978194286,
0.000584150972466743,
0.000615511673938374,
0.000634076321042491,
0.000640480493707285,
0.000635531815048285,
0.000620182130079154,
0.000595498822625910,
0.000562635910625722,
0.000522805526595393,
0.000477250345533527,
0.000427217468328144,
0.000373934206482134,
0.000318586145357320,
0.000262297789959442,
0.000206116021356137,
0.000150996514942755,
9.77931957020622*Math.pow(10,-05),
4.72507320129448*Math.pow(10,-05),
2.21335526688467*Math.pow(10,-19),
-4.34436137200429*Math.pow(10,-05),
-8.26792605240968*Math.pow(10,-05),
-0.000117417268937985,
-0.000147473655218603,
-0.000172762818366640,
-0.000193288755392445,
-0.000209135145928216,
-0.000220454659109724,
-0.000227457830366631,
-0.000230401841893808,
-0.000229579518844779,
-0.000225308824574912,
-0.000217923103587748,
-0.000207762281335220,
-0.000195165186905419,
-0.000180463119163816,
-0.000163974730378486,
-0.000146002255027031,
-0.000126829066580441,
-0.000106718502743356,
-8.59138609597121*Math.pow(10,-05),
-6.46394319050838*Math.pow(10,-05),
-4.31024099779152*Math.pow(10,-05),
-2.14954971090868*Math.pow(10,-05),
-1.25023320567641*Math.pow(10,-19),
};
/**
------------------------------------------------------
*/
}


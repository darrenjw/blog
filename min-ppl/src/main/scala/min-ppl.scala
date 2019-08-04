object MinPpl {

  import breeze.stats.{distributions => bdist}
  import breeze.linalg.DenseVector

  implicit val numParticles = 300
  
  case class Particle[T](v: T, lw: Double) { // value and log-weight
    def map[S](f: T => S): Particle[S] = Particle(f(v), lw)
  }

  trait Prob[T] {
    val particles: Vector[Particle[T]]
    def map[S](f: T => S): Prob[S] = Empirical(particles map (_ map f))
    def flatMap[S](f: T => Prob[S]): Prob[S] = {
      Empirical((particles map (p => {
        f(p.v).particles.map(psi => Particle(psi.v, p.lw + psi.lw))
      })).flatten).resample
    }
    def resample(implicit N: Int): Prob[T] = {
      val lw = particles map (_.lw)
      val mx = lw reduce (math.max(_,_))
      val rw = lw map (lwi => math.exp(lwi - mx))
      val law = mx + math.log(rw.sum/(rw.length))
      val ind = bdist.Multinomial(DenseVector(rw.toArray)).sample(N)
      val newParticles = ind map (i => particles(i))
      Empirical(newParticles.toVector map (pi => Particle(pi.v, law)))
    }
    def cond(ll: T => Double): Prob[T] =
      Empirical(particles map (p => Particle(p.v, p.lw + ll(p.v))))
    def empirical: Vector[T] = resample.particles.map(_.v)
  }

  case class Empirical[T](particles: Vector[Particle[T]]) extends Prob[T]

  def unweighted[T](ts: Vector[T], lw: Double = 0.0): Prob[T] =
    Empirical(ts map (Particle(_, lw)))

  trait Dist[T] extends Prob[T] {
    def ll(obs: T): Double
    def ll(obs: Seq[T]): Double = obs map (ll) reduce (_+_)
    def fit(obs: Seq[T]): Prob[T] =
      Empirical(particles map (p => Particle(p.v, p.lw + ll(obs))))
    def fitQ(obs: Seq[T]): Prob[T] = Empirical(Vector(Particle(obs.head, ll(obs))))
    def fit(obs: T): Prob[T] = fit(List(obs))
    def fitQ(obs: T): Prob[T] = fitQ(List(obs))
  }

  case class Normal(mu: Double, v: Double)(implicit N: Int) extends Dist[Double] {
    lazy val particles = unweighted(bdist.Gaussian(mu, math.sqrt(v)).sample(N).toVector).particles
    def ll(obs: Double) = bdist.Gaussian(mu, math.sqrt(v)).logPdf(obs)
  }

  case class Gamma(a: Double, b: Double)(implicit N: Int) extends Dist[Double] {
    lazy val particles = unweighted(bdist.Gamma(a, 1.0/b).sample(N).toVector).particles
    def ll(obs: Double) = bdist.Gamma(a, 1.0/b).logPdf(obs)
  }

  case class Poisson(mu: Double)(implicit N: Int) extends Dist[Int] {
    lazy val particles = unweighted(bdist.Poisson(mu).sample(N).toVector).particles
    def ll(obs: Int) = bdist.Poisson(mu).logProbabilityOf(obs)
  }

}
